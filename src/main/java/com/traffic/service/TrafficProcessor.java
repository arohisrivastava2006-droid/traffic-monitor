package com.traffic.service;

import com.traffic.dao.AlertDAO;
import com.traffic.dao.TrafficDataDAO;
import com.traffic.exception.InvalidTrafficDataException;
import com.traffic.model.Alert;
import com.traffic.model.Junction;
import com.traffic.model.TrafficData;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Business logic layer: validates readings, stores them, classifies congestion
 * and raises alerts. Uses an ExecutorService so several junctions are
 * processed at the same time.
 */
public class TrafficProcessor {
    private final TrafficDataDAO trafficDAO = new TrafficDataDAO();
    private final AlertDAO alertDAO = new AlertDAO();
    private final TrafficAnalyzer analyzer = new CongestionAnalyzer();

    /** Throws InvalidTrafficDataException when a value is out of range. */
    public static void validate(TrafficData d) throws InvalidTrafficDataException {
        if (d.getJunctionId() <= 0)
            throw new InvalidTrafficDataException("Please choose a junction.");
        if (d.getVehicleCount() < 0)
            throw new InvalidTrafficDataException("Vehicle count cannot be negative.");
        if (d.getTrafficDensity() < 0 || d.getTrafficDensity() > 100)
            throw new InvalidTrafficDataException("Traffic density must be between 0 and 100.");
        if (d.getAverageSpeed() < 0 || d.getAverageSpeed() > 200)
            throw new InvalidTrafficDataException("Average speed must be between 0 and 200 km/h.");
        if (d.getTimestamp() == null)
            throw new InvalidTrafficDataException("Timestamp is missing.");
    }

    /** Validate, store, classify and (optionally) raise alerts. Returns the traffic level. */
    public String processReading(TrafficData d, boolean raiseAlerts, Set<String> alertedKeys)
            throws InvalidTrafficDataException, SQLException {
        validate(d);
        trafficDAO.add(d);
        String level = analyzer.classify(d);
        if (raiseAlerts) {
            raiseAlertsFor(d, level, alertedKeys);
        }
        return level;
    }

    private void raiseAlertsFor(TrafficData d, String level, Set<String> alertedKeys) throws SQLException {
        List<Alert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        if ("SEVERE".equals(level)) {
            alerts.add(new Alert(d.getJunctionId(), "SEVERE_CONGESTION", "CRITICAL",
                    "Severe congestion: density " + d.getTrafficDensity() + "%, speed "
                            + d.getAverageSpeed() + " km/h", now));
        } else if ("HIGH".equals(level)) {
            alerts.add(new Alert(d.getJunctionId(), "HIGH_CONGESTION", "WARNING",
                    "High congestion: density " + d.getTrafficDensity() + "%", now));
        }
        if (d.getAverageSpeed() < 15) {
            alerts.add(new Alert(d.getJunctionId(), "LOW_SPEED", "WARNING",
                    "Average speed unusually low: " + d.getAverageSpeed() + " km/h", now));
        }
        if (d.getTrafficDensity() >= 95) {
            alerts.add(new Alert(d.getJunctionId(), "ABNORMAL_DENSITY", "CRITICAL",
                    "Abnormal traffic density: " + d.getTrafficDensity() + "%", now));
        }
        for (Alert a : alerts) {
            // HashSet-style de-duplication: one alert per junction + type per batch
            if (alertedKeys == null || alertedKeys.add(a.getJunctionId() + ":" + a.getAlertType())) {
                alertDAO.add(a);
            }
        }
    }

    /**
     * Generates sample readings for every junction using multiple threads.
     * Phase 1: one thread per junction creates readings and puts them on a queue.
     * Phase 2: worker threads take events off the queue, validate and store them.
     *
     * @param history true = spread readings across the last 7 days (no alerts),
     *                false = recent readings for "now" (alerts are raised)
     * @return number of readings stored
     */
    public int simulate(List<Junction> junctions, int readingsPerJunction, boolean history) throws Exception {
        return simulate(junctions, readingsPerJunction, history, 7);
    }

    /** Same as above, but history readings are spread over the given number of days. */
    public int simulate(List<Junction> junctions, int readingsPerJunction, boolean history, int days) throws Exception {
        if (junctions.isEmpty()) return 0;
        int threads = Math.min(4, junctions.size());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        Queue<TrafficData> events = new ConcurrentLinkedQueue<>();
        Set<String> alertedKeys = ConcurrentHashMap.newKeySet();
        try {
            // Phase 1: generate
            List<Future<?>> gen = new ArrayList<>();
            for (Junction j : junctions) {
                gen.add(pool.submit(() -> generate(j.getJunctionId(), readingsPerJunction, history, days, events)));
            }
            for (Future<?> f : gen) f.get();

            // Phase 2: process
            List<Future<Integer>> work = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                Callable<Integer> task = () -> {
                    int stored = 0;
                    TrafficData d;
                    while ((d = events.poll()) != null) {
                        try {
                            processReading(d, !history, alertedKeys);
                            stored++;
                        } catch (InvalidTrafficDataException e) {
                            System.err.println("Skipped invalid reading: " + e.getMessage());
                        }
                    }
                    return stored;
                };
                work.add(pool.submit(task));
            }
            int total = 0;
            for (Future<Integer> f : work) total += f.get();
            return total;
        } finally {
            pool.shutdown();
        }
    }

    private void generate(int junctionId, int count, boolean history, int days, Queue<TrafficData> out) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocalDateTime now = LocalDateTime.now();
        double bias = (junctionId % 3) * 8;
        for (int i = 0; i < count; i++) {
            LocalDateTime ts = history
                    ? now.minusDays(r.nextInt(0, days)).withHour(r.nextInt(0, 24)).withMinute(r.nextInt(0, 60))
                    : now.minusMinutes((long) i * 5);
            double density = clamp(rushFactor(ts.getHour()) * 80 + bias + r.nextDouble(-15, 15), 3, 100);
            double speed = clamp(60 - density * 0.55 + r.nextDouble(-6, 6), 5, 80);
            int vehicles = (int) (density * 3 + r.nextInt(0, 25));
            out.add(new TrafficData(junctionId, vehicles, round1(density), round1(speed), ts));
        }
    }

    private static double rushFactor(int hour) {
        if (hour >= 8 && hour <= 10) return 0.85;
        if (hour >= 17 && hour <= 20) return 0.90;
        if (hour >= 11 && hour <= 16) return 0.50;
        if (hour >= 6 && hour <= 7) return 0.50;
        if (hour >= 21) return 0.30;
        return 0.12;
    }

    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}

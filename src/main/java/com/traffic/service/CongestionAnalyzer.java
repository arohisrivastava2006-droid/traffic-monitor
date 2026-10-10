package com.traffic.service;

import com.traffic.model.TrafficData;

/**
 * Classifies a reading as LOW, MODERATE, HIGH or SEVERE.
 * Thresholds are constants so they are easy to configure.
 */
public class CongestionAnalyzer implements TrafficAnalyzer {
    public static final double SEVERE_DENSITY = 80, SEVERE_SPEED = 15;
    public static final double HIGH_DENSITY = 60, HIGH_SPEED = 30;
    public static final double MODERATE_DENSITY = 35;

    @Override
    public String classify(TrafficData d) {
        double density = d.getTrafficDensity();
        double speed = d.getAverageSpeed();
        if (density >= SEVERE_DENSITY && speed < SEVERE_SPEED) return "SEVERE";
        if (density >= HIGH_DENSITY && speed < HIGH_SPEED)     return "HIGH";
        if (density >= SEVERE_DENSITY)                         return "HIGH";
        if (density >= MODERATE_DENSITY)                       return "MODERATE";
        return "LOW";
    }
}

package com.traffic.model;

import com.traffic.service.CongestionAnalyzer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrafficData {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final CongestionAnalyzer ANALYZER = new CongestionAnalyzer();

    private int recordId;
    private int junctionId;
    private String junctionName;
    private int vehicleCount;
    private double trafficDensity;
    private double averageSpeed;
    private LocalDateTime timestamp;

    public TrafficData() { }

    public TrafficData(int junctionId, int vehicleCount, double trafficDensity,
                       double averageSpeed, LocalDateTime timestamp) {
        this.junctionId = junctionId;
        this.vehicleCount = vehicleCount;
        this.trafficDensity = trafficDensity;
        this.averageSpeed = averageSpeed;
        this.timestamp = timestamp;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public int getJunctionId() { return junctionId; }
    public void setJunctionId(int junctionId) { this.junctionId = junctionId; }
    public String getJunctionName() { return junctionName; }
    public void setJunctionName(String junctionName) { this.junctionName = junctionName; }
    public int getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(int vehicleCount) { this.vehicleCount = vehicleCount; }
    public double getTrafficDensity() { return trafficDensity; }
    public void setTrafficDensity(double trafficDensity) { this.trafficDensity = trafficDensity; }
    public double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    /** Formatted timestamp for JSP pages. */
    public String getTimestampText() {
        return timestamp == null ? "" : timestamp.format(FMT);
    }

    /** LOW / MODERATE / HIGH / SEVERE */
    public String getLevel() {
        return ANALYZER.classify(this);
    }
}

package com.traffic.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Alert {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private int alertId;
    private int junctionId;
    private String junctionName;
    private String alertType;
    private String severity;
    private String message;
    private LocalDateTime timestamp;
    private boolean resolved;

    public Alert() { }

    public Alert(int junctionId, String alertType, String severity, String message, LocalDateTime timestamp) {
        this.junctionId = junctionId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }
    public int getJunctionId() { return junctionId; }
    public void setJunctionId(int junctionId) { this.junctionId = junctionId; }
    public String getJunctionName() { return junctionName; }
    public void setJunctionName(String junctionName) { this.junctionName = junctionName; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getTimestampText() {
        return timestamp == null ? "" : timestamp.format(FMT);
    }
}

package com.traffic.model;

public class Junction {
    private int junctionId;
    private String name;
    private String location;
    private TrafficData latest; // most recent reading, may be null

    public Junction() { }

    public Junction(int junctionId, String name, String location) {
        this.junctionId = junctionId;
        this.name = name;
        this.location = location;
    }

    public int getJunctionId() { return junctionId; }
    public void setJunctionId(int junctionId) { this.junctionId = junctionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public TrafficData getLatest() { return latest; }
    public void setLatest(TrafficData latest) { this.latest = latest; }
}

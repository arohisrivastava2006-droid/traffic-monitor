package com.traffic.service;

import com.traffic.model.TrafficData;

/** Abstraction: any class that can classify a traffic reading. */
public interface TrafficAnalyzer {
    String classify(TrafficData data);
}

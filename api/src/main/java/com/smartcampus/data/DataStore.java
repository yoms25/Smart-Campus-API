package com.smartcampus.api.data;

import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class DataStore {
    private static final DataStore instance = new DataStore();
    
    // Thread-safe map to store rooms by their ID
    private Map<String, Room> rooms = new ConcurrentHashMap<>();
    private Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {}

    public static DataStore getInstance() {
        return instance;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }
    
    public Map<String, Sensor> getSensors() {
        return sensors;
    }
    
    public Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
}
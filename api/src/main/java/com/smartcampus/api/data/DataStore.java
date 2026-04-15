package com.smartcampus.api.data;

import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.Sensor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DataStore {
    private static final DataStore instance = new DataStore();
    
    // Thread-safe map to store rooms by their ID
    private Map<String, Room> rooms = new ConcurrentHashMap<>();
    private Map<String, Sensor> sensors = new ConcurrentHashMap<>();

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
}
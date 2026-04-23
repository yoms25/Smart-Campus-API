/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

/**
 *
 * @author user
 */
import com.smartcampus.data.DataStore;
import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    // The constructor receives the context from the parent SensorResource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        // Fetch the list directly using your upgraded DataStore structure
        List<SensorReading> history = DataStore.getInstance().getSensorReadings().get(this.sensorId);
        
        // If there are no readings yet, return an empty list instead of null
        if (history == null) {
            history = new ArrayList<>();
        }
        
        return Response.ok(history).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor parentSensor = DataStore.getInstance().getSensors().get(this.sensorId);
        
        if (parentSensor != null && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is disconnected for maintenance.");
        }

        DataStore.getInstance().getSensorReadings()
                .computeIfAbsent(this.sensorId, k -> new ArrayList<>())
                .add(reading);

        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue()); 
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}

package com.smartcampus.resources;

import com.smartcampus.data.DataStore;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private Map<String, Sensor> sensors = DataStore.getInstance().getSensors();
    private Map<String, Room> rooms = DataStore.getInstance().getRooms();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(sensors.values());
        
        // Filter by type if the query parameter is provided
        if (type != null && !type.trim().isEmpty()) {
            sensorList = sensorList.stream()
                    .filter(s -> type.equalsIgnoreCase(s.getType()))
                    .collect(Collectors.toList());
        }
        
        return Response.ok(sensorList).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getRoomId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid sensor data").build();
        }

        // Integrity Check: Ensure the roomId exists in the system
        if (!rooms.containsKey(sensor.getRoomId())) {
            throw new com.smartcampus.exceptions.LinkedResourceNotFoundException("Linked room does not exist.");
        }

        sensors.put(sensor.getId(), sensor);
        
        // Add sensor ID to the Room's list of sensors to link them
        rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        
        System.out.println("DEBUG: Added sensor " + sensor.getId() + " to room " + sensor.getRoomId());
        System.out.println("DEBUG: Room " + sensor.getRoomId() + " now has " + rooms.get(sensor.getRoomId()).getSensorIds().size() + " sensors.");

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId())).entity(sensor).build();
    }
    
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        if (!DataStore.getInstance().getSensors().containsKey(sensorId)) {
            throw new LinkedResourceNotFoundException("Sensor does not exist."); 
        }
        return new SensorReadingResource(sensorId);
    }
}
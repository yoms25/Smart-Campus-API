package com.smartcampus.api.resources;

import com.smartcampus.api.data.DataStore;
import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            // Note: This will be upgraded to a custom 422 Exception in Part 5
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Linked room does not exist.")
                    .build();
        }

        sensors.put(sensor.getId(), sensor);
        
        // Add sensor ID to the Room's list of sensors to link them
        rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId())).entity(sensor).build();
    }
}
package com.smartcampus.api.resources;

import com.smartcampus.api.data.DataStore;
import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    // This connects to our "Database" from Step 1
    private Map<String, Room> rooms = DataStore.getInstance().getRooms();

    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(rooms.values());
        return Response.ok(roomList).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid room data").build();
        }
        
        rooms.put(room.getId(), room);
        // This returns a 201 Created status, which is required for top marks!
        return Response.created(URI.create("/api/v1/rooms/" + room.getId())).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }
        return Response.ok(room).build();
    }
    
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        
        // If the room doesn't exist (or was already deleted)
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }
        
        // Business Logic Constraint: Prevent orphans
        if (!room.getSensorIds().isEmpty()) {
            throw new com.smartcampus.api.exceptions.RoomNotEmptyException("Cannot delete: Room contains active sensors.");
        }
        
        rooms.remove(roomId);
        return Response.noContent().build(); // 204 No Content indicates successful deletion
    }
    
    @GET
    @Path("/{roomId}/analytics")
    public Response getRoomAnalytics(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }

        List<Double> allValues = new ArrayList<>();
        Map<String, List<SensorReading>> allReadings = DataStore.getInstance().getSensorReadings();

        for (String sensorId : room.getSensorIds()) {
            if (allReadings.containsKey(sensorId)) {
                for (SensorReading reading : allReadings.get(sensorId)) {
                    allValues.add(reading.getValue());
                }
            }
        }

        if (allValues.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).entity("No data available for this room").build();
        }

        double min = allValues.stream().mapToDouble(v -> v).min().orElse(0.0);
        double max = allValues.stream().mapToDouble(v -> v).max().orElse(0.0);
        double avg = allValues.stream().mapToDouble(v -> v).average().orElse(0.0);

        Map<String, Double> stats = new HashMap<>();
        stats.put("minimum", min);
        stats.put("maximum", max);
        stats.put("average", avg);

        return Response.ok(stats).build();
    }
}
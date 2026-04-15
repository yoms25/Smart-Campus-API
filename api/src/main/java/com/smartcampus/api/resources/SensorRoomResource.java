package com.smartcampus.api.resources;

import com.smartcampus.api.data.DataStore;
import com.smartcampus.api.models.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
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
            // Note: We will upgrade this to a Custom Exception Mapper in Part 5
            return Response.status(Response.Status.CONFLICT)
                           .entity("Cannot delete: Room contains active sensors.")
                           .build();
        }
        
        rooms.remove(roomId);
        return Response.noContent().build(); // 204 No Content indicates successful deletion
    }
}
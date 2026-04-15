package com.smartcampus.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryInfo() {
        Map<String, Object> metadata = new HashMap<>();
        
        // Essential API metadata
        metadata.put("version", "1.0.0");
        metadata.put("contact", "admin@smartcampus.westminster.ac.uk"); // Update with your student email
        
        // Map of primary resource collections
        Map<String, String> resourceLinks = new HashMap<>();
        resourceLinks.put("rooms", "/api/v1/rooms");
        resourceLinks.put("sensors", "/api/v1/sensors");
        
        metadata.put("resources", resourceLinks);

        return Response.ok(metadata).build();
    }
}
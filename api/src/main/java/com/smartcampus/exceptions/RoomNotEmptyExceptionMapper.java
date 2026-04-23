/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.api.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author user
 */
@Provider // This annotation registers the mapper with JAX-RS
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Returns a structured JSON body with a 409 status code
        return Response.status(Response.Status.CONFLICT)
                .entity("{\"error\": \"Conflict\", \"message\": \"" + exception.getMessage() + "\"}")
                .type("application/json")
                .build();
    }
}

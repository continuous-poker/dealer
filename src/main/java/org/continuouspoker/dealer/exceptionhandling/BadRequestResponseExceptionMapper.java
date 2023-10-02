package org.continuouspoker.dealer.exceptionhandling;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestResponseExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(final IllegalArgumentException onfe) {
        return Response.status(Response.Status.BAD_REQUEST).entity(onfe.getMessage()).build();
    }
    
}

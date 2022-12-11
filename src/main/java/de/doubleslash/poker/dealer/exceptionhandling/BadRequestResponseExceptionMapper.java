package de.doubleslash.poker.dealer.exceptionhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BadRequestResponseExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(final IllegalArgumentException onfe) {
        return Response.status(Response.Status.BAD_REQUEST).entity(onfe.getMessage()).build();
    }
    
}

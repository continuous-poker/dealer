package org.continuouspoker.dealer.exceptionhandling;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.continuouspoker.dealer.exceptionhandling.exceptions.ObjectNotFoundException;

@Provider
public class ResponseExceptionMapper implements ExceptionMapper<ObjectNotFoundException> {

    @Override
    public Response toResponse(final ObjectNotFoundException onfe) {
        return Response.status(Response.Status.NOT_FOUND).entity(onfe.getMessage()).build();
    }

}

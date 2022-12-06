package de.doubleslash.poker.dealer.exceptionHandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.doubleslash.poker.dealer.exceptionHandling.exceptions.ObjectNotFoundException;

@Provider
public class ResponseExceptionMapper implements ExceptionMapper<ObjectNotFoundException> {

    @Override
    public Response toResponse(final ObjectNotFoundException onfe)
    {
        return Response.status(Response.Status.NOT_FOUND).entity(onfe.getMessage()).build();
    }
}

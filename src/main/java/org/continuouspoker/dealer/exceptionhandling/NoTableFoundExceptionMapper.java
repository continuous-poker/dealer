package org.continuouspoker.dealer.exceptionhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.continuouspoker.dealer.exceptionhandling.exceptions.NoTableStateFoundException;

@Provider
public class NoTableFoundExceptionMapper implements ExceptionMapper<NoTableStateFoundException> {

    @Override
    public Response toResponse(final NoTableStateFoundException onfe) {
        return Response.status(Response.Status.NOT_FOUND).entity(onfe.getMessage()).build();
    }

}

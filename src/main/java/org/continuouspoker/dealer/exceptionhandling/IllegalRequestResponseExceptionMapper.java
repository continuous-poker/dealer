package org.continuouspoker.dealer.exceptionhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IllegalRequestResponseExceptionMapper implements ExceptionMapper<IllegalStateException> {

    @Override
    public Response toResponse(final IllegalStateException onfe) {
        return Response.status(Response.Status.BAD_REQUEST).entity(onfe.getMessage()).build();
    }

}

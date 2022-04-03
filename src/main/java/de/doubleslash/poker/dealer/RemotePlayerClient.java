package de.doubleslash.poker.dealer;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import de.doubleslash.poker.dealer.data.Table;

public interface RemotePlayerClient {

    @POST
    @Path("/")
    Integer getExtensionsById(Table id);

}

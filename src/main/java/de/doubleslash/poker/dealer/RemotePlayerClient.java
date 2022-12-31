package de.doubleslash.poker.dealer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.doubleslash.poker.dealer.data.PlayerBet;
import de.doubleslash.poker.dealer.data.Table;

public interface RemotePlayerClient {

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    PlayerBet getExtensionsById(Table id);

}

package org.continuouspoker.dealer;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.continuouspoker.dealer.data.PlayerBet;
import org.continuouspoker.dealer.data.Table;

public interface RemotePlayerClient {

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    PlayerBet getExtensionsById(Table table);

}

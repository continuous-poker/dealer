package org.continuouspoker.dealer.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import lombok.RequiredArgsConstructor;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.data.Table;
import org.continuouspoker.dealer.exceptionhandling.exceptions.ObjectNotFoundException;
import org.continuouspoker.dealer.game.Game;

@Path("/games")
@Produces("application/json")
@RequiredArgsConstructor
public class ManagementController {

    public static final String PARAM_GAME_ID = "gameId";
    private final ManagementService service;

    @POST
    @Path("/manage/{gameId}/players")
    public void registerPlayer(@PathParam(PARAM_GAME_ID) final long gameId,
            @QueryParam("playerUrl") final String playerUrl, @QueryParam("teamName") final String teamName)
            throws ObjectNotFoundException {
        service.registerPlayer(gameId, playerUrl, teamName);
    }

    @DELETE
    @Path("/manage/{gameId}/players")
    public void removePlayer(@PathParam(PARAM_GAME_ID) final long gameId,
            @QueryParam("teamName") final String teamName) {
        service.removePlayer(gameId, teamName);
    }

    @GET
    @Path("/{gameId}/players")
    public Collection<String> getPlayers(@PathParam(PARAM_GAME_ID) final long gameId) {
        return service.getPlayers(gameId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/manage/")
    public long start(@FormParam("name") final String name) {
        return service.start(name);
    }

    @GET
    @Path("/{gameId}")
    public String getStatus(@PathParam(PARAM_GAME_ID) final long gameId) {
        return service.getStatus(gameId);
    }

    @GET
    @Path("/{gameId}/score")
    public Map<String, Long> getScore(@PathParam(PARAM_GAME_ID) final long gameId) {
        return service.getScore(gameId);
    }

    @GET
    @Path("/")
    public Collection<Game> listGames() {
        return service.listGames();
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @DELETE
    @Path("/manage/{gameId}")
    public void delete(@PathParam(PARAM_GAME_ID) final long gameId) {
        service.delete(gameId);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PUT
    @Path("/manage/{gameId}")
    public void toggleRun(@PathParam(PARAM_GAME_ID) final long gameId) {
        service.toggleRun(gameId);
    }

    @GET
    @Path("/{gameId}/log/{timestamp}")
    public List<LogEntry> getLogSince(@PathParam(PARAM_GAME_ID) final long gameId,
            @PathParam("timestamp") final String timestamp) throws ObjectNotFoundException {
        return service.getLogSince(gameId, timestamp);
    }

    @GET
    @Path("/{gameId}/log")
    public List<LogEntry> filterLog(@PathParam(PARAM_GAME_ID) final long gameId,
            @QueryParam("from") final String limitFrom, @QueryParam("to") final String limitTo,
            @QueryParam("tableId") final Long tableId, @QueryParam("limit") final Integer limit,
            @QueryParam("order") final String order) throws ObjectNotFoundException {
        return service.filterLog(gameId, limitFrom, limitTo, tableId, limit, order);
    }

    @GET
    @Path("/{gameId}/tournament/{tournamentId}")
    public Table getStateOfTournament(@PathParam(PARAM_GAME_ID) final long gameId,
            @PathParam("tournamentId") final long tournamentId) throws ObjectNotFoundException {
        return service.getStateOfTournament(gameId, tournamentId);
    }

    @GET
    @Path("/{gameId}/tournament/{tournamentId}/round/{roundId}")
    public Table getStateOfRound(@PathParam(PARAM_GAME_ID) final long gameId,
            @PathParam("tournamentId") final long tournamentId, @PathParam("roundId") final long roundId)
            throws ObjectNotFoundException {
        return service.getStateOfRound(gameId, tournamentId, roundId);
    }

    @GET
    @Path("/{gameId}/history")
    public Map<Long, Map<Long, List<String>>> getGameHistory(@PathParam(PARAM_GAME_ID) final long gameId)
            throws ObjectNotFoundException {
        return service.getGameHistory(gameId);
    }

}

/*
 * Copyright © 2024 DoubleSlash Net-Business GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.continuouspoker.dealer.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
        // empty for now, takes up too much resources
        return Map.of();
    }

    @GET
    @Path("/{gameId}/scoreHistory")
    public Map<String, List<ScoreHistoryEntry>> getLogSince(@PathParam(PARAM_GAME_ID) final long gameId) {
        return service.getScoreHistory(gameId);
    }

    @GET
    @Path("/{gameId}/latestIds")
    public String getLatestTournamentAndRound(@PathParam(PARAM_GAME_ID) final long gameId) throws ObjectNotFoundException {
        return service.getLatestTournamentAndRound(gameId);
    }

}

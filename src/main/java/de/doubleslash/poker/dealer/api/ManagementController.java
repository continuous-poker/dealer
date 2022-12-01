package de.doubleslash.poker.dealer.api;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.doubleslash.poker.dealer.DummyPlayer;
import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.GameManager;
import de.doubleslash.poker.dealer.LogEntry;
import de.doubleslash.poker.dealer.RemotePlayer;
import de.doubleslash.poker.dealer.Team;
import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.GameHistory;
import de.doubleslash.poker.dealer.data.Table;
import de.doubleslash.poker.dealer.game.Game;

@Path("/games")
public class ManagementController {

    private final GameManager gameState;
    private final GameLogger log;

    public ManagementController(final GameManager gameState, final GameLogger log) {
        this.gameState = gameState;
        this.log = log;
    }

    @POST
    @Path("/{gameId}/players")
    public void registerPlayer(@PathParam("gameId") final long gameId, @QueryParam("playerUrl") final String playerUrl,
            @QueryParam("teamName") final String teamName) {
        final Optional<Game> game = gameState.getGame(gameId);
        game.ifPresent(g -> g.addPlayer(new Team(teamName, new RemotePlayer(playerUrl))));
    }

    @DELETE
    @Path("/{gameId}/players")
    public void removePlayer(@PathParam("gameId") final long gameId, @QueryParam("teamName") final String teamName) {
        final Optional<Game> game = gameState.getGame(gameId);
        game.ifPresent(g -> g.getTeams()
                             .stream()
                             .filter(team -> team.getName().equals(teamName))
                             .findFirst()
                             .ifPresent(g::removePlayer));
    }

    @GET
    @Path("/{gameId}/players")
    public Collection<String> getPlayers(@PathParam("gameId") final long gameId) {
        final Optional<Game> game = gameState.getGame(gameId);
        if (game.isPresent()) {
            final List<Team> teams = game.get().getTeams();
            return teams.stream().map(Team::getName).toList();
        }
        return Collections.emptyList();
    }

    @GET
    @Path("/testrun")
    public long testRun() {
        final Collection<Team> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            players.add(new Team("Team" + i, new DummyPlayer()));
        }
        return gameState.runSingleGame("Testrun Game " + new Random().nextInt(), players).getGameId();
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/")
    public long start(@FormParam("name") final String name) {
        return gameState.createNewGame(name);
    }

    @GET
    @Path("/{gameId}")
    public String getStatus(@PathParam("gameId") final long gameId) {
        return gameState.isRunning(gameId) ? "running" : "stopped";
    }

    @GET
    @Path("/{gameId}/score")
    public Map<String, Long> getScore(@PathParam("gameId") final long gameId) {
        final Map<String, Long> map = new HashMap<>();

        gameState.getGame(gameId)
                 .ifPresent(game -> game.getTeams().forEach(team -> map.put(team.getName(), team.getScore())));

        return map;
    }

    @GET
    @Path("/")
    public Collection<Game> listGames() {
        return gameState.getGames();
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @DELETE
    @Path("/{gameId}")
    public void delete(@PathParam("gameId") final long gameId) {
        gameState.delete(gameId);
        log.delete(gameId);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PUT
    @Path("/{gameId}")
    public void toggleRun(@PathParam("gameId") final long gameId) {
        if (gameState.isRunning(gameId)) {
            gameState.pause(gameId);
        } else {
            gameState.resume(gameId);
        }
    }

    @GET
    @Path("/{gameId}/log/{timestamp}")
    public List<LogEntry> getLogSince(@PathParam("gameId") final long gameId,
            @PathParam("timestamp") final String timestamp) {
        final List<LogEntry> list = log.getLog(gameId).orElse(Collections.emptyList());

        return list.stream().filter(entry -> entry.getTimestamp().isAfter(ZonedDateTime.parse(timestamp))).toList();
    }

    @GET
    @Path("/{gameId}/log")
    public List<LogEntry> filterLog(@PathParam("gameId") final long gameId, @QueryParam("from") final String from,
            @QueryParam("to") final String to, @QueryParam("tableId") final Long tableId,
            @QueryParam("limit") final Integer limit, @QueryParam("order") final String order) {
        final List<LogEntry> list = new ArrayList<>(log.getLog(gameId).orElse(Collections.emptyList()));

        final Predicate<LogEntry> isAfter = entry -> from == null || entry.getTimestamp()
                                                                          .isAfter(ZonedDateTime.parse(from));
        final Predicate<LogEntry> isBefore = entry -> to == null || entry.getTimestamp()
                                                                         .isBefore(ZonedDateTime.parse(to));
        final Predicate<LogEntry> isTable = entry -> tableId == null || entry.getTableId() == tableId;

        if ("desc".equals(order)) {
            Collections.reverse(list);
        }

        Stream<LogEntry> logEntryStream = list.stream().filter(isAfter.and(isBefore).and(isTable));

        if (limit != null && limit > 0) {
            logEntryStream = logEntryStream.limit(limit);
        }

        return logEntryStream.toList();
    }

    @GET
    @Path("/{gameId}/table/{tableId}")
    public Table getTable(@PathParam("gameId") final long gameId, @PathParam("tableId") final long tableId) {
        return gameState.getGame(gameId).map(game -> game.getTables().get(tableId)).orElse(null);
    }

    @GET
    @Path("/{gameId}/gameHistory")
    public Map<Long, Map<Long, List<String>>> getGameHistory(@PathParam("gameId") final long gameId) {
        return gameState.getGame(gameId).map(Game::getGameHistory).map(GameHistory::getGameLogHistory).orElse(null);
    }
}

package de.doubleslash.poker.dealer.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import de.doubleslash.poker.dealer.game.Game;

public class ManagementController {

   private final GameManager gameState;
   private final GameLogger log;

   public ManagementController(final GameManager gameState, final GameLogger log) {
      this.gameState = gameState;
      this.log = log;
   }

   @POST
   @Path("/games/{gameId}/players")
   public void registerPlayer(@PathParam("gameId") final long gameId,
         @QueryParam("playerUrl") final String playerUrl, @QueryParam("teamName") final String teamName) {
      final Optional<Game> game = gameState.getGame(gameId);
      game.ifPresent(g -> g.addPlayer(new Team(teamName, new RemotePlayer(playerUrl))));
   }

   @GET
   @Path("/games/{gameId}/players")
   public Collection<String> getPlayers(@PathParam("gameId") final long gameId) {
      final Optional<Game> game = gameState.getGame(gameId);
      if (game.isPresent()) {
         final List<Team> teams = game.get().getTeams();
         return teams.stream().map(Team::getName).collect(Collectors.toList());
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
   @Path("/games")
   public long start(@FormParam("name") final String name) {
      return gameState.createNewGame(name);
   }

   @GET
   @Path("/games/{gameId}")
   public String getStatus(@PathParam("gameId") final long gameId) {
      return gameState.isRunning(gameId) ? "running" : "stopped";
   }

   @GET
   @Path("/games/{gameId}/score")
   public Map<String, Long> getScore(@PathParam("gameId") final long gameId) {
      final Map<String, Long> map = new HashMap<>();

      gameState.getGame(gameId)
               .ifPresent(game -> game.getTeams().forEach(team -> map.put(team.getName(), team.getScore())));

      return map;
   }

   @GET
   @Path("/games")
   public Collection<Game> listGames() {
      return gameState.getGames();
   }

   //@PreAuthorize("hasRole('ADMIN')")
   @DELETE
   @Path("/games/{gameId}")
   public void delete(@PathParam("gameId") final long gameId) {
      gameState.delete(gameId);
      log.delete(gameId);
   }

   //@PreAuthorize("hasRole('ADMIN')")
   @PUT
   @Path("/games/{gameId}")
   public void toggleRun(@PathParam("gameId") final long gameId) {
      if (gameState.isRunning(gameId)) {
         gameState.pause(gameId);
      } else {
         gameState.resume(gameId);
      }
   }

   @GET
   @Path("/games/{gameId}/log/{timestamp}")
   public List<LogEntry> getLogSince(@PathParam("gameId") final long gameId,
         @PathParam("timestamp") final LocalDateTime timestamp) {
      final List<LogEntry> list = log.getLog(gameId).orElse(Collections.emptyList());

      return list.stream().filter(entry -> entry.getTimestamp().isAfter(timestamp)).collect(Collectors.toList());
   }

   @GET
   @Path("/games/{gameId}/log")
   public List<LogEntry> filterLog(@PathParam("gameId") final long gameId,
         @QueryParam("from") final LocalDateTime from,
         @QueryParam("to") final LocalDateTime to,
         @QueryParam("table_id") final Long tableId) {
      final List<LogEntry> list = log.getLog(gameId).orElse(Collections.emptyList());

      final Predicate<LogEntry> isAfter = entry -> from == null || entry.getTimestamp().isAfter(from);
      final Predicate<LogEntry> isBefore = entry -> to == null || entry.getTimestamp().isBefore(to);
      final Predicate<LogEntry> isTable = entry -> tableId == null || entry.getTableId() == tableId;

      return list.stream().filter(isAfter.and(isBefore).and(isTable)).collect(Collectors.toList());
   }
}

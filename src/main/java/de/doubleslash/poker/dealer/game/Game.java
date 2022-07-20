package de.doubleslash.poker.dealer.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.Team;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Status;
import de.doubleslash.poker.dealer.data.Table;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Game implements Runnable {

   private static final int START_SMALLBLIND = 5;
   private static final int START_STACK = 100;
   private static final int POINTS = 1;

   private final List<Team> teams;
   private final GameLogger logger;
   private final long gameId;
   private int tableId = 0;
   private final String name;

   public Game(final long gameId, final String name, final GameLogger log) {
      this.gameId = gameId;
      this.name = name;
      this.teams = new ArrayList<>();
      this.logger = log;
   }

   @Override
   public void run() {
      try {
         final List<Player> players = initPlayers();
         final long id = tableId++;
         final Table table = new Table(id, players, START_SMALLBLIND);

         while (isMoreThanOnePlayerLeft(players)) {
            new GameRound(players, table, logger, gameId).run();
            sleep(3, TimeUnit.SECONDS);
         }
         addWinnerPoints(players, id);

      } catch (final Exception e) {
         log.error("Unexpected error in game", e);
      }
   }

   private void sleep(final int sleeptime, final TimeUnit unit) {
      try {
         Thread.sleep(unit.toMillis(sleeptime));
      } catch (InterruptedException e) {
         log.error("Got interrupted in sleep", e);
         Thread.currentThread().interrupt();
      }
   }

   private void addWinnerPoints(final List<Player> players, final long tableId) {
      players.stream()
             .filter(s -> !s.getStatus()
                            .equals(Status.OUT))
             .map(this::getTeam)
             .forEach(team -> {
                team.addToScore(POINTS);
                logger.log(gameId, tableId, "Player %s won the table!", team.getName());
             });
   }

   private Team getTeam(final Player player) {
      return teams.stream()
                  .filter(t -> t.getName()
                                .equals(player.getName()))
                  .findFirst()
                  .orElseThrow(IllegalStateException::new);
   }

   private boolean isMoreThanOnePlayerLeft(final List<Player> players) {
      return players.stream()
                    .map(Player::getStatus)
                    .filter(s -> !s.equals(Status.OUT))
                    .count() > 1;
   }

   private List<Player> initPlayers() {
      final List<Player> players = new ArrayList<>();
      for (final Team team : teams) {
         final Player player = new Player(team.getName(), Status.ACTIVE, START_STACK, 0, new ArrayList<>(),
               team.getProvider());
         players.add(player);
      }
      return players;
   }

   public long getGameId() {
      return gameId;
   }

   public List<Team> getTeams() {
      return teams;
   }

   public void addPlayer(final Team team) {
      teams.add(team);
   }
   public void removePlayer(final Team team) {
      teams.remove(team);
   }

   public String getName() {
      return name;
   }

}

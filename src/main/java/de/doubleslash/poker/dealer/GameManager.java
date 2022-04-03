package de.doubleslash.poker.dealer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.doubleslash.poker.dealer.game.Game;

@ApplicationScoped
public class GameManager {

   private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
   private final GameLogger log;

   private final Map<Game, ScheduledFuture<?>> games = new HashMap<>();

   @Inject
   public GameManager(final GameLogger log) {
      this.log = log;
   }

   public long createNewGame(final String name) {
      final long gameId = generateGameId();
      final Game game = new Game(gameId, name, log);
      games.put(game, null);
      return gameId;
   }

   public void startGame(final long gameId) {
      resume(gameId);
   }

   private long generateGameId() {
      return new Random().nextInt(Integer.MAX_VALUE);
   }

   public synchronized void resume(final long gameId) {
      getGame(gameId).ifPresent(game -> {
         if (games.get(game) == null || games.get(game)
                                             .isCancelled()) {
            games.put(game, scheduler.scheduleAtFixedRate(game, 0, 10, TimeUnit.SECONDS));
         }
      });
   }

   public synchronized void pause(final long gameId) {
      getGame(gameId).ifPresent(game -> games.get(game)
                                             .cancel(true));
   }

   public void delete(final long gameId) {
      getGame(gameId).ifPresent(games::remove);
   }

   public Game runSingleGame(final String name, final Collection<Team> players) {
      final long generateGameId = generateGameId();
      final Game game = new Game(generateGameId, name, log);
      players.forEach(game::addPlayer);
      game.run();
      return game;
   }

   public boolean isRunning(final long gameId) {
      final Optional<Game> game = getGame(gameId);
      if (game.isPresent()) {
         final Game key = game.get();
         final ScheduledFuture<?> scheduledFuture = games.get(key);
         if (scheduledFuture != null) {
            return !scheduledFuture.isCancelled();
         }
      }
      return false;
   }

   public Collection<Long> getGameIds() {
      return games.keySet()
                  .stream()
                  .map(Game::getGameId)
                  .collect(Collectors.toSet());
   }

   public Collection<Game> getGames() {
      return games.keySet();
   }

   public Optional<Game> getGame(final long gameId) {
      return games.keySet()
                  .parallelStream()
                  .filter(g -> g.getGameId() == gameId)
                  .findAny();
   }

}

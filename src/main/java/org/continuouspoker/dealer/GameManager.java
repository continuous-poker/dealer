package org.continuouspoker.dealer;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

import org.continuouspoker.dealer.game.Game;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class GameManager {

    private static final int GAME_INTERVAL_SECONDS = 10;
    @ConfigProperty(name = "gameround.sleep.duration")
    /* package */ Duration gameRoundSleepDuration;

    @ConfigProperty(name = "step.sleep.duration")
    /* package */ Duration stepSleepDuration;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    private final Random random = new Random();

    private final Map<Game, ScheduledFuture<?>> games = new HashMap<>();

    public long createNewGame(final String name) {
        final long gameId = generateGameId();
        final Game game = new Game(gameId, name, gameRoundSleepDuration, stepSleepDuration);
        games.put(game, null);
        return gameId;
    }

    private long generateGameId() {
        return random.nextInt(Integer.MAX_VALUE);
    }

    public void resume(final long gameId) {
        synchronized (this) {
            getGame(gameId).ifPresent(game -> {
                if (games.get(game) == null || games.get(game).isCancelled()) {
                    games.put(game, scheduler.scheduleAtFixedRate(game, 0, GAME_INTERVAL_SECONDS, TimeUnit.SECONDS));
                }
            });
        }
    }

    public void pause(final long gameId) {
        synchronized (this) {
            getGame(gameId).flatMap(this::getScheduledGame).ifPresent(g -> g.cancel(false));
        }
    }

    private Optional<ScheduledFuture<?>> getScheduledGame(final Game game) {
        return Optional.ofNullable(games.get(game));
    }

    public void delete(final long gameId) {
        getGame(gameId).ifPresent(game -> {
            getScheduledGame(game).ifPresent(g -> g.cancel(true));
            games.remove(game);
        });
    }

    public Game runSingleGame(final String name, final Collection<Team> players) {
        final long generateGameId = generateGameId();
        final Game game = new Game(generateGameId, name, Duration.ZERO, Duration.ZERO);
        players.forEach(game::addPlayer);
        game.run();
        return game;
    }

    public boolean isRunning(final long gameId) {
        return getGame(gameId).flatMap(this::getScheduledGame).map(g -> !g.isCancelled()).orElse(false);
    }

    public Collection<Game> getGames() {
        return Collections.unmodifiableSet(games.keySet());
    }

    public Optional<Game> getGame(final long gameId) {
        return games.keySet().parallelStream().filter(g -> g.getGameId() == gameId).findAny();
    }

}

package org.continuouspoker.dealer;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.daos.GameDAO;
import org.continuouspoker.dealer.persistence.daos.LogEntryDAO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
public class GameManager {

    @ConfigProperty(name = "tournament.sleep.duration")
    /* package */ Duration tournamentSleepDuration;

    @ConfigProperty(name = "gameround.sleep.duration")
    /* package */ Duration gameRoundSleepDuration;

    @ConfigProperty(name = "step.sleep.duration")
    /* package */ Duration stepSleepDuration;

    @ConfigProperty(name = "game.executor.poolsize")
    /* package */ int executorPoolsize;

    private final GameDAO dao;
    private final LogEntryDAO logEntryDAO;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(executorPoolsize);
    private final Map<Game, ScheduledFuture<?>> games = new TreeMap<>(Comparator.comparing(Game::getName));

    @PostConstruct
        /* package */ void initialize() {
        final Optional<List<Game>> gameList = dao.loadGames();
        gameList.ifPresent(l -> l.forEach(g -> games.put(g, null)));
    }

    @Scheduled(delayed = "10s", every = "10s")
        /* package */ void store() {
        dao.storeGames(games.keySet());
    }

    public long createNewGame(final String name) {
        Game game = new Game(0L, name, gameRoundSleepDuration, stepSleepDuration, dao, logEntryDAO);
        dao.createGame(game);
        games.put(game, null);
        return game.getGameId();
    }

    public void resume(final long gameId) {
        synchronized (this) {
            getGame(gameId).ifPresent(game -> {
                if (games.get(game) == null || games.get(game).isCancelled()) {
                    games.put(game, scheduler.scheduleAtFixedRate(game, 0, tournamentSleepDuration.getSeconds(),
                            TimeUnit.SECONDS));
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

    public boolean isRunning(final long gameId) {
        return getGame(gameId).flatMap(this::getScheduledGame).map(g -> !g.isCancelled()).orElse(false);
    }

    public Collection<Game> getGames() {
        return Collections.unmodifiableSet(games.keySet());
    }

    public Optional<Game> getGame(final long gameId) {
        return games.keySet().parallelStream().filter(g -> g.getGameId() == gameId).findAny();
    }

    public Team createNewPlayer(final String teamName, final String playerUrl) {
        return dao.createTeam(teamName, playerUrl);
    }

}

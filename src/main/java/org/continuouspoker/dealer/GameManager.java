package org.continuouspoker.dealer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.GameBE;
import org.continuouspoker.dealer.persistence.TeamBE;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class GameManager {

    private static final int GAME_INTERVAL_SECONDS = 10;
    @ConfigProperty(name = "gameround.sleep.duration")
    /* package */ Duration gameRoundSleepDuration;

    @ConfigProperty(name = "step.sleep.duration")
    /* package */ Duration stepSleepDuration;

    @ConfigProperty(name = "persistence.path")
    /* package */ String storagePath;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    private final Random random = new Random();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Game, ScheduledFuture<?>> games = new HashMap<>();

    @PostConstruct
    /* package */ void initialize() {
        try {
            final var path = Path.of(storagePath);
            if (Files.exists(path)) {
                final var serializedList = Files.readString(path);
                final List<GameBE> gameList = objectMapper.readerForListOf(GameBE.class).readValue(serializedList);
                gameList.forEach(g -> {
                    final var game = new Game(g.gameId(), g.name(), gameRoundSleepDuration, stepSleepDuration);
                    g.teams().forEach(t -> {
                        final var team = new Team(t.name(), new RemotePlayer(t.providerUrl()));
                        team.addToScore(t.score());
                        game.addPlayer(team);
                    });
                    games.put(game, null);
                });
            }
        } catch (IOException e) {
            log.error("Could not load stored games on startup, continuing with empty list.", e);
        }
    }

    @Scheduled(delayed = "10s", every = "10s")
    /* package */ void store() {
        try {
            final var path = Path.of(storagePath);
            final var listToSerialize = games.keySet()
                                             .stream()
                                             .map(g -> new GameBE(g.getGameId(), g.getName(), g.getTeams()
                                                                                               .stream()
                                                                                               .map(t -> new TeamBE(
                                                                                                       t.getScore(),
                                                                                                       t.getName(),
                                                                                                       t.getProvider()
                                                                                                        .getUrl()))
                                                                                               .toList()))
                                             .toList();
            final var serializedList = objectMapper.writeValueAsString(listToSerialize);
            Files.writeString(path, serializedList, StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error("Could not store game states, progress might get lost on restart.", e);
        }
    }

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

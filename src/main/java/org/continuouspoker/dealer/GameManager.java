/*
 * Copyright © 2020 - 2024 Jan Kreutzfeld
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
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.daos.GameDAO;
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

    @Inject
    GameDAO gameDao;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(executorPoolsize);
    private final Map<Game, ScheduledFuture<?>> games = new TreeMap<>(Comparator.comparing(Game::getName));

    @PostConstruct
        /* package */ void initialize() {
        final List<Game> gameList = gameDao.loadGames();
        gameList.forEach(g -> games.put(g, null));
    }

    @Scheduled(delayed = "10s", every = "10s")
        /* package */ void store() {
        gameDao.storeGames(games.keySet());
    }

    public long createNewGame(final String name) {
        Game game = new Game(0L, name, gameRoundSleepDuration, stepSleepDuration);
        gameDao.createGame(game);
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
        return gameDao.createTeam(teamName, playerUrl);
    }

}

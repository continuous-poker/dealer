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

package org.continuouspoker.dealer.game;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Table;
import org.continuouspoker.dealer.persistence.daos.LogEntryDAO;

@Slf4j
@RequiredArgsConstructor
public class Tournament {

    private static final int START_SMALL_BLIND = 5;
    private static final int START_STACK = 100;
    private static final int POINTS = 1;

    private final long gameId;
    @Getter
    private final long tournamentId;
    private final List<Team> teams;

    private final Duration timeBetweenGameRounds;
    private final Duration timeBetweenSteps;
    private final List<LogEntry> gameLog = new ArrayList<>();
    private final List<GameRound> gameRounds = new ArrayList<>();
    @Inject
    LogEntryDAO logEntryDAO;

    @SuppressWarnings({ "PMD.AvoidInstantiatingObjectsInLoops",
                        "PMD.AvoidCatchingGenericException"
    })
    public void run() {
        try {
            final List<Player> players = initPlayers();

            final var table = new Table(tournamentId, players, START_SMALL_BLIND);

            long roundNumber = 0;
            while (isMoreThanOnePlayerLeft(players)) {
                roundNumber++;
                final GameRound gameRound = new GameRound(roundNumber, players, table, timeBetweenSteps);
                gameRounds.add(gameRound);
                gameRound.run();
                sleep();
            }
            addWinnerPoints(players, roundNumber);

        } catch (final Exception e) {
            log.error("Unexpected error in game", e);
        }
    }

    public Stream<LogEntry> getHistory() {
        final Stream<LogEntry> roundLogs = List.copyOf(gameRounds).stream()
                                                     .flatMap(s -> s.getHistory()
                                                                    .map(e -> new LogEntry(e.timestamp(), gameId,
                                                                            tournamentId, e.roundNumber(),
                                                                            e.message())));
        storeLogEntries(roundLogs.toList());
        return Stream.concat(roundLogs, List.copyOf(gameLog).stream());
    }

    private boolean isMoreThanOnePlayerLeft(final List<Player> players) {
        return players.stream().map(Player::getStatus).filter(s -> !s.equals(Status.OUT)).count() > 1;
    }

    private void addWinnerPoints(final List<Player> players, final long roundNumber) {
        players.stream().filter(s -> !s.getStatus().equals(Status.OUT)).map(this::getTeam).forEach(team -> {
            team.addToScore(POINTS);
            storeLogEntry(new LogEntry(ZonedDateTime.now(), gameId, tournamentId, roundNumber,
                    String.format("Player %s won the tournament!", team.getName())));
        });
    }

    private List<Player> initPlayers() {
        return teams.stream()
                    .map(team -> new Player(team.getName(), Status.ACTIVE, START_STACK, 0, team.getProvider()))
                    .toList();
    }

    private Team getTeam(final Player player) {
        return teams.stream()
                    .filter(t -> t.getName().equals(player.getName()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
    }

    private void sleep() {
        try {
            Thread.sleep(timeBetweenGameRounds.toMillis());
        } catch (InterruptedException e) {
            log.error("Got interrupted in sleep", e);
            Thread.currentThread().interrupt();
        }
    }

    public Optional<Table> getLatestTableState() {
        if (gameRounds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(gameRounds.get(gameRounds.size() - 1).getTable());
    }

    public Optional<Table> getTableStateOfGameRound(final long roundId) {
        return gameRounds.stream().filter(r -> r.getRoundId() == roundId).map(GameRound::getTable).findFirst();
    }

    public long getLatestRound() {
        if (gameRounds.isEmpty()) {
            return 0;
        }
        return gameRounds.get(gameRounds.size() - 1).getRoundId();
    }

    public void storeLogEntry(LogEntry logEntry) {
        logEntryDAO.storeLogEntry(logEntry);
    }

    public void storeLogEntries(final List<LogEntry> logEntries) {
        logEntryDAO.storeLogEntries(logEntries);
    }
}

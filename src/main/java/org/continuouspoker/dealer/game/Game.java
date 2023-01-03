package org.continuouspoker.dealer.game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.continuouspoker.dealer.GameLogger;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.data.GameHistory;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Game implements Runnable {

    private static final int START_SMALL_BLIND = 5;
    private static final int START_STACK = 100;
    private static final int POINTS = 1;
    private final List<Team> teams = new ArrayList<>();

    @Getter
    private final Map<Long, Table> tables = new HashMap<>();

    @Getter
    private final GameHistory gameHistory = new GameHistory();
    @Getter
    private final long gameId;
    @Getter
    private final String name;
    private final GameLogger logger;
    private final Duration timeBetweenGameRounds;
    private final Duration timeBetweenSteps;
    private int tableId;

    @Override
    @SuppressWarnings({ "PMD.AvoidInstantiatingObjectsInLoops",
                        "PMD.AvoidCatchingGenericException"
    })
    public void run() {
        try {
            final List<Player> players = initPlayers();

            final long currentTableId = nextTableId();
            Table table = new Table(currentTableId, players, START_SMALL_BLIND,
                    logMsg -> logger.log(gameId, currentTableId, 0, logMsg));

            tables.put(currentTableId, table);

            while (isMoreThanOnePlayerLeft(players)) {

                table = new GameRound(players, table, logger, gameId, timeBetweenSteps).run();
                tables.put(currentTableId, table);
                sleep();
            }
            addWinnerPoints(players, currentTableId);
            gameHistory.addTableRoundHistory(currentTableId, logger.getCopyGameLog(gameId));

        } catch (final Exception e) {
            log.error("Unexpected error in game", e);
        }
    }

    private long nextTableId() {
        return tableId++;
    }

    private void sleep() {
        try {
            Thread.sleep(timeBetweenGameRounds.toMillis());
        } catch (InterruptedException e) {
            log.error("Got interrupted in sleep", e);
            Thread.currentThread().interrupt();
        }
    }

    private void addWinnerPoints(final List<Player> players, final long tableId) {
        players.stream().filter(s -> !s.getStatus().equals(Status.OUT)).map(this::getTeam).forEach(team -> {
            team.addToScore(POINTS);
            logger.log(gameId, tableId, 0, "Player %s won the table!", team.getName());
        });
    }

    private Team getTeam(final Player player) {
        return teams.stream()
                    .filter(t -> t.getName().equals(player.getName()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
    }

    private boolean isMoreThanOnePlayerLeft(final List<Player> players) {
        return players.stream().map(Player::getStatus).filter(s -> !s.equals(Status.OUT)).count() > 1;
    }

    private List<Player> initPlayers() {
        return teams.stream()
                    .map(team -> new Player(team.getName(), Status.ACTIVE, START_STACK, 0, team.getProvider()))
                    .toList();
    }

    public void addPlayer(final Team team) {
        teams.add(team);
    }

    public void removePlayer(final Team team) {
        teams.remove(team);
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }
}
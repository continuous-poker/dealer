package de.doubleslash.poker.dealer.game;

import java.sql.SQLOutput;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationContext;
import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.LogEntry;
import de.doubleslash.poker.dealer.Team;
import de.doubleslash.poker.dealer.data.GameHistory;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Status;
import de.doubleslash.poker.dealer.data.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;

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
    private int tableId = 0;

    @Override
    public void run() {
        try {
            final List<Player> players = initPlayers();

            final long id = nextTableId();
            Table table = new Table(id, players, START_SMALL_BLIND,
                    logMsg -> logger.log(gameId, id, 0, logMsg));

            tables.put(id, table);

            while (isMoreThanOnePlayerLeft(players)) {

                table = new GameRound(players, table, logger, gameId).run();
                tables.put(id, table);
                sleep();
            }
            addWinnerPoints(players, id);
            gameHistory.addTableRoundHistory(id, logger.getCopyGameLog());

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
        final List<Player> players = new ArrayList<>();
        for (final Team team : teams) {
            final Player player = new Player(team.getName(), Status.ACTIVE, START_STACK, 0, team.getProvider());
            players.add(player);
        }
        return players;
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

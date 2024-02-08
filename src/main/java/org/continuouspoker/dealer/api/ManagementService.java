package org.continuouspoker.dealer.api;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.continuouspoker.dealer.GameManager;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.data.Table;
import org.continuouspoker.dealer.exceptionhandling.exceptions.NoTableStateFoundException;
import org.continuouspoker.dealer.exceptionhandling.exceptions.ObjectNotFoundException;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.game.Tournament;
import org.continuouspoker.dealer.persistence.daos.GameDAO;
import org.continuouspoker.dealer.persistence.entities.ScoreRecordBE;
import org.continuouspoker.dealer.persistence.entities.TeamScoreRecordBE;
import org.continuouspoker.dealer.persistence.daos.LogEntryDAO;

@ApplicationScoped
@RequiredArgsConstructor
public class ManagementService {
    private static final int MAX_NUMBER_OF_PLAYERS = 10;

    @Inject
    GameDAO gameDAO;

    @Inject
    LogEntryDAO logEntryDAO;

    private final GameManager gameState;

    public void registerPlayer(final long gameId, final String playerUrl, final String teamName)
            throws ObjectNotFoundException {
        final Optional<Game> game = gameState.getGame(gameId);
        final int teamListLength = gameState.getGame(gameId)
                                            .map(Game::getTeams)
                                            .map(List::size)
                                            .orElseThrow(ObjectNotFoundException::new);

        if (teamListLength < MAX_NUMBER_OF_PLAYERS) {
            game.ifPresent(g -> g.addPlayer(gameState.createNewPlayer(teamName, playerUrl)));
        } else {
            throw new IllegalArgumentException("Too many players, cant add player: " + teamName + "!");
        }
    }

    public void removePlayer(final long gameId, final String teamName) {
        final Optional<Game> game = gameState.getGame(gameId);
        game.ifPresent(g -> g.getTeams()
                             .stream()
                             .filter(team -> teamName.equals(team.getName()))
                             .findFirst()
                             .ifPresent(g::removePlayer));
    }

    public Collection<String> getPlayers(final long gameId) {
        final Optional<Game> game = gameState.getGame(gameId);
        if (game.isPresent()) {
            final List<Team> teams = game.get().getTeams();
            return teams.stream().map(Team::getName).toList();
        }
        return Collections.emptyList();
    }

    public long start(final String name) {
        return gameState.createNewGame(name);
    }

    public String getStatus(final long gameId) {
        return "{\"state\":\"" + (gameState.isRunning(gameId) ? "running" : "stopped") + "\"}";
    }

    public Map<String, Long> getScore(final long gameId) {
        final Map<String, Long> map = new HashMap<>();

        gameState.getGame(gameId)
                 .ifPresent(game -> game.getTeams().forEach(team -> map.put(team.getName(), team.getScore())));

        return map;
    }

    public Collection<Game> listGames() {
        return gameState.getGames();
    }

    public void delete(final long gameId) {
        gameState.delete(gameId);
    }

    public void toggleRun(final long gameId) {
        if (gameState.isRunning(gameId)) {
            gameState.pause(gameId);
        } else {
            gameState.resume(gameId);
        }
    }

    public List<LogEntry> getLogSince(final long gameId, final String timestamp, int logLimit) {
        return logEntryDAO.findLogsSince(gameId, timestamp, logLimit);
    }

    public List<LogEntry> getLogByGameId(final long gameId, int logLimit) {
        return logEntryDAO.findLogsByGameId(gameId, logLimit);
    }

    public List<LogEntry> getLogByTournamentId(final long tournamentId, int logLimit) {
        return logEntryDAO.findLogsByTournamentId(tournamentId, logLimit);
    }

    public List<LogEntry> getLogByRoundId(final long roundId, int logLimit) {
        return logEntryDAO.findLogsByRoundId(roundId, logLimit);
    }

    public List<LogEntry> filterLog(final long gameId, final String limitFrom, final String limitTo, final Long tableId,
            final Integer limit, final String order) throws ObjectNotFoundException {

        return gameState.getGame(gameId).map(game -> {
            final Predicate<LogEntry> isAfter = entry -> limitFrom == null || entry.getTimestamp()
                                                                                   .isAfter(ZonedDateTime.parse(
                                                                                           limitFrom));
            final Predicate<LogEntry> isBefore = entry -> limitTo == null || entry.getTimestamp()
                                                                                  .isBefore(
                                                                                          ZonedDateTime.parse(limitTo));
            final Predicate<LogEntry> isTable = entry -> tableId == null || entry.getTournamentId() == tableId;

            final List<LogEntry> logs = new ArrayList<>(
                    game.getFullHistory().filter(isAfter.and(isBefore).and(isTable)).toList());
            if ("desc".equals(order)) {
                Collections.reverse(logs);
            }
            if (limit != null && limit > 0) {
                return logs.stream().limit(limit).toList();
            }
            return logs;
        }).orElseThrow(ObjectNotFoundException::new);
    }

    public Table getStateOfTournament(final long gameId, final long tournamentId) throws ObjectNotFoundException {
        return gameState.getGame(gameId)
                        .map(game -> game.getTournaments()
                                         .stream()
                                         .filter(t -> t.getTournamentId() == tournamentId)
                                         .map(Tournament::getLatestTableState)
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .findFirst()
                                         .orElseThrow(NoTableStateFoundException::new))
                        .orElseThrow(() -> new ObjectNotFoundException("Game or tournament not found!"));
    }

    public Table getStateOfRound(final long gameId, final long tournamentId, final long roundId)
            throws ObjectNotFoundException {
        return gameState.getGame(gameId)
                        .map(game -> game.getTournaments()
                                         .stream()
                                         .filter(t -> t.getTournamentId() == tournamentId)
                                         .map(t -> t.getTableStateOfGameRound(roundId))
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .findFirst()
                                         .orElseThrow(NoTableStateFoundException::new))
                        .orElseThrow(() -> new ObjectNotFoundException("Game or tournament not found!"));
    }

    public Map<Long, Map<Long, List<String>>> getGameHistory(final long gameId) throws ObjectNotFoundException {
        final Stream<LogEntry> gameLogs = gameState.getGame(gameId)
                                                   .map(Game::getFullHistory)
                                                   .orElseThrow(
                                                           () -> new ObjectNotFoundException("GameHistory not found!"));
        // <TournamentId, <RoundId, Message>>
        return gameLogs.collect(Collectors.groupingBy(LogEntry::getTournamentId,
                Collectors.groupingBy(LogEntry::getRoundId,
                        Collectors.mapping(LogEntry::getMessage, Collectors.toList()))));
    }

    public Map<String, List<ScoreHistoryEntry>> getScoreHistory(final long gameId) {
        final Map<String, List<ScoreHistoryEntry>> result = new TreeMap<>();
        List<ScoreRecordBE> scores = gameDAO.loadScores(gameId);
        scores.forEach(rec -> {
            final Set<TeamScoreRecordBE> teamScores = rec.getTeamScores();
            for (final TeamScoreRecordBE score : teamScores) {
                final List<ScoreHistoryEntry> teamList = getTeamList(score, result);
                teamList.add(new ScoreHistoryEntry(rec.getCreationTimestamp(), score.getScore()));
            }
        });
        return result;
    }

    private List<ScoreHistoryEntry> getTeamList(final TeamScoreRecordBE score,
            final Map<String, List<ScoreHistoryEntry>> teamLists) {
        return teamLists.computeIfAbsent(score.getTeam().getName(), teamName -> new ArrayList<>());
    }

    public String getLatestTournamentAndRound(final long gameId) throws ObjectNotFoundException {
        return gameState.getGame(gameId)
                        .map(game -> """
                                {
                                   "tournamentId": %s,
                                   "roundId": %s
                                }
                                """.formatted(game.getTournamentId() - 1,
                                game.getTournaments().get(game.getTournaments().size() - 1).getLatestRound()))
                        .orElseThrow(() -> new ObjectNotFoundException("GameHistory not found!"));
    }
}

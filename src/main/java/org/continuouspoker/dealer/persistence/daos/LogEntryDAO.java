package org.continuouspoker.dealer.persistence.daos;

import java.time.ZonedDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.persistence.entities.LogEntryBE;
import org.continuouspoker.dealer.persistence.mappers.LogEntryMapper;
import org.mapstruct.factory.Mappers;

@ApplicationScoped
public class LogEntryDAO {
    private static final int GAME_LOG_LIMIT = 50;
    LogEntryMapper logEntryMapper = Mappers.getMapper(LogEntryMapper.class);

    @Transactional
    public void storeLogEntries(final List<LogEntry> logEntries) {
        logEntries.stream().map(entry -> logEntryMapper.toEntity(entry)).forEach(log -> log.persist());
    }

    public List<LogEntry> findLogsSince(final long gameId, final String timestamp) {
        return findLogsByGameId(gameId).stream()
                                       .filter(entry -> entry.getTimestamp()
                                                             .isAfter(ZonedDateTime.parse(timestamp)))
                                       .toList();
    }

    public List<LogEntry> findLogsByGameId(final long gameId) {
        final PanacheQuery<PanacheEntityBase> query = LogEntryBE.find("gameId",
                Sort.by("gameId", Sort.Direction.Descending), gameId);
        query.range(0, GAME_LOG_LIMIT);
        List<LogEntryBE> logs = query.list();
        return logs.stream().map(logEntryMapper::toDto).toList();
    }

    public List<LogEntry> findLogsByTournamentId(final long tournamentId) {
        final PanacheQuery<PanacheEntityBase> query = LogEntryBE.find("tournamentId",
                Sort.by("tournamentId", Sort.Direction.Descending), tournamentId);
        query.range(0, GAME_LOG_LIMIT);
        List<LogEntryBE> logs = query.list();
        return logs.stream().map(logEntryMapper::toDto).toList();
    }

    public List<LogEntry> findLogsByRoundId(final long roundId) {
        final PanacheQuery<PanacheEntityBase> query = LogEntryBE.find("roundId",
                Sort.by("roundId", Sort.Direction.Descending), roundId);
        query.range(0, GAME_LOG_LIMIT);
        List<LogEntryBE> logs = query.list();
        return logs.stream().map(logEntryMapper::toDto).toList();
    }
}

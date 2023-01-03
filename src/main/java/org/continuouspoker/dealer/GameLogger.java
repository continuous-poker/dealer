package org.continuouspoker.dealer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class GameLogger {

    private static final int LOG_MAX_LENGTH = 10_000;
    private final Map<Long, List<LogEntry>> logs = new ConcurrentHashMap<>();

    public void log(final long gameId, final long tableId, final long roundId, final String msg, final Object... args) {
        final LogEntry logEntry = new LogEntry(ZonedDateTime.now(), gameId, tableId, roundId, String.format(msg, args));
        add(logEntry);

        log.info(logEntry.toString());
    }

    private void add(final LogEntry logEntry) {
        final List<LogEntry> gameLog = logs.computeIfAbsent(logEntry.getGameId(), k -> new ArrayList<>());
        gameLog.add(logEntry);
        if (gameLog.size() > LOG_MAX_LENGTH) {
            gameLog.remove(0);
        }
    }

    public List<LogEntry> getCopyGameLog(final long gameId) {
        return new ArrayList<>(logs.get(gameId));
    }


    public Optional<List<LogEntry>> getLog(final long gameId) {
        return Optional.ofNullable(logs.get(gameId));
    }

    public void delete(final long gameId) {
        logs.remove(gameId);
    }
}

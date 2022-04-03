package de.doubleslash.poker.dealer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;


import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class GameLogger {

   private final Map<Long, List<LogEntry>> logs = new HashMap<>();

   public void log(final long gameId, final long tableId, final String msg, final Object... args) {
      final List<LogEntry> gameLog = logs.computeIfAbsent(gameId, k -> new ArrayList<>());
      final LogEntry logEntry = new LogEntry(LocalDateTime.now(), gameId, tableId, String.format(msg, args));
      gameLog.add(logEntry);
      log.info(logEntry.toString());
   }

   public Optional<List<LogEntry>> getLog(final long gameId) {
      return Optional.ofNullable(logs.get(gameId));
   }

   public void delete(final long gameId) {
      logs.remove(gameId);
   }
}

package de.doubleslash.poker.dealer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LogEntry {
   private final LocalDateTime timestamp;
   private final long gameId;
   private final long tableId;
   private final String message;
}

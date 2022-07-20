package de.doubleslash.poker.dealer;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LogEntry {
   private final ZonedDateTime timestamp;
   private final long gameId;
   private final long tableId;
   private final String message;
}

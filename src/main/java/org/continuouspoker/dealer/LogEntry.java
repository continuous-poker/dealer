package org.continuouspoker.dealer;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LogEntry {
    private final ZonedDateTime timestamp;
    private final long gameId;
    private final long tableId;
    private final long roundId;
    private final String message;
}

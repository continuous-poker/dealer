package org.continuouspoker.dealer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class LogEntry {
    private final ZonedDateTime timestamp;
    private final long gameId;
    private final long tournamentId;
    private final long roundId;
    private final String message;

    public String getMessage() {
        return "[" + timestamp.format(DateTimeFormatter.ISO_TIME) + "] " + message;
    }
}

package org.continuouspoker.dealer;

import java.time.ZonedDateTime;

public record GameRoundLogEntry(long roundNumber, ZonedDateTime timestamp, String message) {
}

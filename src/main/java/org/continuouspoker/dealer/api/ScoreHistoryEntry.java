package org.continuouspoker.dealer.api;

import java.time.Instant;

public record ScoreHistoryEntry(Instant creationTimestamp, long score) {
}

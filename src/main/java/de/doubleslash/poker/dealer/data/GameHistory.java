package de.doubleslash.poker.dealer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.doubleslash.poker.dealer.LogEntry;
import lombok.Getter;

public class GameHistory {

    @Getter
    private final Map<Long, Map<Long, List<String>>> gameLogHistory = new HashMap<>(); //Map<tableId, Map<roundId, roundMessages>>

    public void addTableRoundHistory(final Long tableId, final List<LogEntry> gameHistory) {
        final Map<Long, List<String>> tableLogHistory = createMapForTable(gameHistory);

        gameLogHistory.put(tableId, tableLogHistory);
    }

    public Map<Long, List<String>> createMapForTable(final List<LogEntry> gameHistory) {

        final Map<Long, List<String>> tableMessages = new HashMap<>();
        final List<String> roundMessages = new ArrayList<>();

        long roundId = 1L;

        for (final LogEntry logEntry : gameHistory) {

            if (logEntry.getRoundId() == roundId || logEntry.getRoundId() == 0) {
                roundMessages.add(logEntry.getMessage());
            } else {
                final List<String> copyMessageList = new ArrayList<>(roundMessages);
                tableMessages.put(roundId, copyMessageList);
                roundMessages.clear();
                roundId = logEntry.getRoundId();
                roundMessages.add(logEntry.getMessage());
            }
        }
        return tableMessages;
    }
}

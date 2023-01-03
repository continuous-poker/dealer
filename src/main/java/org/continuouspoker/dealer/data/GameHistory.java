package org.continuouspoker.dealer.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import org.continuouspoker.dealer.LogEntry;

public class GameHistory {

    @Getter
    private final List<TableLog> gameLogHistory = new ArrayList<>();

    public void addTableRoundHistory(final Long tableId, final List<LogEntry> gameHistory) {
        final List<RoundLog> tableLogHistory = createMapForTable(tableId, gameHistory);

        gameLogHistory.add(new TableLog(tableId, tableLogHistory));
    }

    public List<RoundLog> createMapForTable(final Long tableId, final List<LogEntry> gameHistory) {

        final List<RoundLog> tableMessages = new ArrayList<>();

        gameHistory.stream()
                   .filter(log -> log.getTableId() == tableId)
                   .collect(Collectors.groupingBy(LogEntry::getRoundId))
                   .forEach((roundId, messages) -> tableMessages.add(
                           new RoundLog(roundId, messages.stream().map(LogEntry::getMessage).toList())));

        return tableMessages;
    }

    public record TableLog(long tableId, List<RoundLog> roundLogs) {
    }

    public record RoundLog(long roundId, List<String> messages) {
    }
}

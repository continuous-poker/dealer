package org.continuouspoker.dealer.persistence.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;

import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.persistence.entities.LogEntryBE;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LogEntryMapperTest {

    private LogEntryMapper logEntryMapper = Mappers.getMapper(LogEntryMapper.class);

    @Test
    public void testEntityToDto() {
        LogEntryBE logEntryBE = new LogEntryBE();
        logEntryBE.setGameId(1L);
        logEntryBE.setTournamentId(2L);
        logEntryBE.setRoundId(3L);

        LogEntry logEntry = logEntryMapper.toDto(logEntryBE);

        assertEquals(logEntry.getGameId(), logEntryBE.getGameId());
        assertEquals(logEntry.getTournamentId(), logEntryBE.getTournamentId());
        assertEquals(logEntry.getRoundId(), logEntryBE.getRoundId());
    }

    @Test
    public void testDtoToEntity() {
        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), 1L, 2L, 3L, "testee");

        LogEntryBE logEntryBE = logEntryMapper.toEntity(logEntry);

        assertEquals(logEntryBE.getGameId(), logEntry.getGameId());
        assertEquals(logEntryBE.getTournamentId(), logEntry.getTournamentId());
        assertEquals(logEntryBE.getRoundId(), logEntry.getRoundId());
    }
}
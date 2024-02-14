/*
 * Copyright © 2020-2024 doubleSlash Net-Business GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.continuouspoker.dealer.persistence.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;

import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.persistence.entities.LogEntryBE;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LogEntryMapperTest {

    private final LogEntryMapper logEntryMapper = Mappers.getMapper(LogEntryMapper.class);

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
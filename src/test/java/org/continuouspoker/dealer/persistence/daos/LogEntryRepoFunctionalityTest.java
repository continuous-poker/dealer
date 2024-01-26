/*
 * Copyright © 2024 DoubleSlash Net-Business GmbH
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

package org.continuouspoker.dealer.persistence.daos;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.persistence.entities.LogEntryBE;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestTransaction
class LogEntryRepoFunctionalityTest {

    LogEntryDAO testee = new LogEntryDAO();

    @Test
    void givenAListOfLogEntries_whenStoreLogEntriesIsCalled_thenPersist() {
        testee.storeLogEntries(mockLogEntries());

        PanacheQuery<PanacheEntityBase> query = LogEntryBE.find("gameId", 1L);
        List<LogEntryBE> logs = query.list();

        assertNotNull(query);
        assertEquals(1, logs.size());
    }

    @Test
    void givenAGameId_whenFindLogsByGameIdMethodIsCalled_thenOnlyReturnOneLog() {
        LogEntry log = mock(LogEntry.class);
        LogEntry falseLog = mock(LogEntry.class);
        when(log.getGameId()).thenReturn(10L);
        when(falseLog.getGameId()).thenReturn(5L);

        testee.storeLogEntries(List.of(log, falseLog));

        List<LogEntry> result = testee.findLogsByGameId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getGameId());
    }

    @Test
    void shouldReturnEmptyList_whenFindLogsByGameIdMethodIsCalled() {
        testee.storeLogEntries(mockLogEntries());

        List<LogEntry> result = testee.findLogsByGameId(0L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void givenATournamentId_whenFindLogsByTournamentId_thenReturnLogEntries() {
        testee.storeLogEntries(mockLogEntries());

        List<LogEntry> result = testee.findLogsByTournamentId(2L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyList_whenFindLogsByTournamentIdMethodIsCalled() {
        LogEntry log = mock(LogEntry.class);
        when(log.getTournamentId()).thenReturn(5L);

        testee.storeLogEntries(List.of(log));

        List<LogEntry> result = testee.findLogsByTournamentId(6L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void givenARoundId_whenFindLogsByRoundId_thenReturnLogEntries() {
        testee.storeLogEntries(mockLogEntries());

        List<LogEntry> result = testee.findLogsByRoundId(3L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    private List<LogEntry> mockLogEntries() {
        LogEntry log1 = mock(LogEntry.class);
        when(log1.getGameId()).thenReturn(1L);
        when(log1.getTournamentId()).thenReturn(2L);
        when(log1.getRoundId()).thenReturn(3L);

        LogEntry log2 = mock(LogEntry.class);
        when(log2.getGameId()).thenReturn(10L);
        when(log2.getTournamentId()).thenReturn(2L);
        when(log2.getRoundId()).thenReturn(5L);
        return List.of(log1, log2);
    }


}
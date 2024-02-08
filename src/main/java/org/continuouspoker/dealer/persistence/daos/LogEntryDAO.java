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

package org.continuouspoker.dealer.persistence.daos;

import java.time.ZonedDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.persistence.entities.LogEntryBE;
import org.continuouspoker.dealer.persistence.mappers.LogEntryMapper;

@ApplicationScoped
public class LogEntryDAO {
    @Inject
    LogEntryMapper logEntryMapper;

    @Transactional
    public void storeLogEntry(final LogEntry logEntry) {
        final LogEntryBE log = logEntryMapper.toEntity(logEntry);
        log.persist();
    }

    @Transactional
    public void storeLogEntries(final List<LogEntry> logEntries) {
        logEntries.stream().map(entry -> logEntryMapper.toEntity(entry)).forEach(log -> log.persist());
    }

    public List<LogEntry> findLogsSince(final long gameId, final String timestamp, int logLimit) {
        return findLogsByGameId(gameId, logLimit).stream()
                                       .filter(entry -> entry.getTimestamp()
                                                             .isAfter(ZonedDateTime.parse(timestamp)))
                                       .toList();
    }

    public List<LogEntry> findLogsByGameId(final long gameId, int logLimit) {
        final PanacheQuery<PanacheEntityBase> query = LogEntryBE.find("gameId",
                Sort.by("gameId", Sort.Direction.Descending), gameId);
        query.range(0, logLimit);
        List<LogEntryBE> logs = query.list();
        return logs.stream().map(logEntryMapper::toDto).toList();
    }

    public List<LogEntry> findLogsByTournamentId(final long tournamentId, int logLimit) {
        final PanacheQuery<PanacheEntityBase> query = LogEntryBE.find("tournamentId",
                Sort.by("tournamentId", Sort.Direction.Descending), tournamentId);
        query.range(0, logLimit);
        List<LogEntryBE> logs = query.list();
        return logs.stream().map(logEntryMapper::toDto).toList();
    }

    public List<LogEntry> findLogsByRoundId(final long roundId, int logLimit) {
        final PanacheQuery<PanacheEntityBase> query = LogEntryBE.find("roundId",
                Sort.by("roundId", Sort.Direction.Descending), roundId);
        query.range(0, logLimit);
        List<LogEntryBE> logs = query.list();
        return logs.stream().map(logEntryMapper::toDto).toList();
    }
}

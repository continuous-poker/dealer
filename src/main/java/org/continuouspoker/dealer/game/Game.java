/*
 * Copyright © 2020 - 2024 Jan Kreutzfeld
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

package org.continuouspoker.dealer.game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.persistence.daos.GameDAO;

@Slf4j
@RequiredArgsConstructor
public class Game implements Runnable {

    private static final int TOURNAMENT_LIMIT = 5;
    private final List<Team> teams = new ArrayList<>();

    @Getter
    @JsonIgnore
    private final List<Tournament> tournaments = new ArrayList<>();

    @Getter
    private final long gameId;
    @Getter
    private final String name;
    private final Duration timeBetweenGameRounds;
    private final Duration timeBetweenSteps;

    @Inject
    @JsonIgnore
    GameDAO gameDAO;

    @Getter
    @JsonIgnore
    private int tournamentId;

    @Override
    public void run() {

        final Tournament tournament = new Tournament(gameId, tournamentId++, teams, timeBetweenGameRounds,
                timeBetweenSteps);
        tournaments.add(tournament);
        while (tournaments.size() > TOURNAMENT_LIMIT) {
            tournaments.remove(0);
        }
        tournament.run();

        gameDAO.storeScores(this);
    }

    @JsonIgnore
    public Stream<LogEntry> getFullHistory() {
        return List.copyOf(tournaments).stream().flatMap(Tournament::getHistory);
    }

    public void addPlayer(final Team team) {
        teams.add(team);
    }

    public void removePlayer(final Team team) {
        teams.remove(team);
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }
}

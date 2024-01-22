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
package org.continuouspoker.dealer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import org.continuouspoker.dealer.game.Game;
import org.assertj.core.api.Assertions;
import org.continuouspoker.dealer.persistence.GameDAO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GameManagerTest {

    @Test
    void testGameRun() {
        final Collection<Team> players = new ArrayList<>();
        players.add(new Team(0L,"team1", (table, logger) -> 0));
        players.add(new Team(1L, "team2", (table, logger) -> Integer.MAX_VALUE));

        final GameDAO dao = Mockito.mock(GameDAO.class);

        final Game testgame = new Game(0L, "testgame", Duration.ZERO, Duration.ZERO, dao);
        players.forEach(testgame::addPlayer);
        testgame.run();

        Assertions.assertThat(testgame.getTeams()).hasSize(2);
        assertThat(testgame.getTeams().stream().mapToLong(Team::getScore).sum()).isOne();
    }

}

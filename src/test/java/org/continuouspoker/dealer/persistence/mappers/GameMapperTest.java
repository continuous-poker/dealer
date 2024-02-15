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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.continuouspoker.dealer.RemotePlayer;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.daos.GameDAO;
import org.continuouspoker.dealer.persistence.daos.LogEntryDAO;
import org.continuouspoker.dealer.persistence.entities.GameBE;
import org.continuouspoker.dealer.persistence.entities.TeamBE;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class GameMapperTest {

    private final GameMapper testee = Mappers.getMapper(GameMapper.class);

    @Test
    void testEntityToModel() {
        TeamBE teamBE = new TeamBE();
        teamBE.setName("X");
        teamBE.setScore(1L);
        teamBE.setProviderUrl("http://remotePlayer");

        GameBE gameBE = new GameBE("game", List.of(teamBE));

        Game game = testee.toDto(gameBE);

        assertEquals(game.getName(), gameBE.getName());
        assertEquals(game.getTeams().size(), 1);
    }

    @Test
    void testModelToEntity() {
        // Mocks
        Team team = mock(Team.class);
        RemotePlayer player = mock(RemotePlayer.class);
        GameDAO gameDAO = mock(GameDAO.class);
        LogEntryDAO logEntryDAO = mock(LogEntryDAO.class);

        // Method stubbing
        when(team.getName()).thenReturn("teamX");
        when(team.getProvider()).thenReturn(player);
        when(team.getProvider().getUrl()).thenReturn("http://remotePlayer");

        Game game = new Game(0L, "gameName", Duration.parse("PT1S"), Duration.parse("PT1S"), gameDAO, logEntryDAO);
        game.addPlayer(team);

        GameBE gameBE = testee.toEntity(game);
        List<TeamBE> teamsBE = gameBE.getTeams();

        assertAll(() -> assertEquals("gameName", gameBE.getName()),
                () -> assertNotNull(teamsBE),
                () -> assertEquals("teamX", teamsBE.get(0).getName()),
                () -> assertEquals("http://remotePlayer", teamsBE.get(0).getProviderUrl()));
    }

    @Test
    void convertTeamToTeamBE() {
        // Mock dependencies
        RemotePlayer player = mock(RemotePlayer.class);
        when(player.getUrl()).thenReturn("http://remotePlayer");

        Team team = new Team(0L, "testTeam", player);
        team.addToScore(10);

        TeamBE teamBE = testee.teamToTeamBE(team);

        assertAll(
                () -> assertNotNull(teamBE),
                () -> assertEquals("testTeam", teamBE.getName()),
                () -> assertEquals(10, teamBE.getScore()),
                () -> assertEquals("http://remotePlayer", teamBE.getProviderUrl())
        );
    }
}
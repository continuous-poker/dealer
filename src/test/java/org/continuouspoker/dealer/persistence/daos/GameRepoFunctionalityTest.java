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

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.continuouspoker.dealer.RemotePlayer;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.entities.GameBE;
import org.continuouspoker.dealer.persistence.entities.ScoreRecordBE;
import org.continuouspoker.dealer.persistence.entities.TeamBE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestTransaction
class GameRepoFunctionalityTest {

    @Inject
    GameDAO testee;
    Game game1;
    Game game2;

    @BeforeEach
    void setup() {
        LogEntryDAO logEntryDAO = mock(LogEntryDAO.class);
        game1 = new Game(0L, "game_test_1", Duration.parse("PT1S"), Duration.parse("PT1S"), testee, logEntryDAO);
        game2 = new Game(0L, "game_test_2", Duration.parse("PT1S"), Duration.parse("PT1S"), testee, logEntryDAO);
    }

    @Test
    void givenGameInstance_whenCreateGameMethodIsCalled_thenStoreInDatabase() {
        // Arrange
        testee.createGame(game1);

        PanacheQuery<GameBE> result = GameBE.findAll();
        List<GameBE> games = result.list();

        assertNotNull(result);
        assertEquals(game1.getName(), games.get(0).getName());
    }

    @Test
    void givenListOfGameInstances_whenCreateMethodIsCalled_thenStoreAllInDatabase() {
        // Arrange
        Set<Game> games = Set.of(game1, game2);

        testee.storeGames(games);

        PanacheQuery<GameBE> result = GameBE.findAll(Sort.by("name", Sort.Direction.Ascending));
        List<GameBE> gameList = result.list();

        // Assert
        assertTrue(!gameList.isEmpty());
        assertEquals("game_test_1", gameList.get(0).getName());
        assertEquals("game_test_2", gameList.get(1).getName());
    }

    @Test
    void whenLoadGamesMethodIsCalled_thenReturnGames() {
        // Arrange
        testee.createGame(game1);

        List<Game> result = testee.loadGames();

        assertNotNull(result);

        Game g = result.get(0);
        assertEquals("game_test_1", g.getName());
    }

    @Test
    void givenTeamInstance_whenCreateTeamMethodIsCalled_thenStoreInDatabase() {
        // Arrange
        testee.createTeam("team_test", "http://localhost:8081");

        PanacheQuery<TeamBE> result = TeamBE.findAll();
        List<TeamBE> teams = result.list();

        assertNotNull(result);
        assertEquals("team_test", teams.get(0).getName());
        assertEquals("http://localhost:8081", teams.get(0).getProviderUrl());
    }

    @Test
    void givenGameInstance_whenStoreScoreMethodIsCalled_thenAddScoreAndPersist() {
        Game game = mockGameClass();

        testee.storeScores(game);

        PanacheQuery<ScoreRecordBE> result = ScoreRecordBE.findAll();
        List<ScoreRecordBE> scores = result.list();

        assertNotNull(result);
        assertEquals(2L, scores.get(0).getGameId());
    }

    @Test
    void givenAGameId_whenLoadScoresIsCalled_thenReturnScores() {
        // Arrange
        Game game = mockGameClass();
        testee.storeScores(game);

        List<ScoreRecordBE> scores = testee.loadScores(game.getGameId());
        assertNotNull(scores);

        ScoreRecordBE score = scores.get(0);
        assertEquals(2L, score.getGameId());
    }

    private Game mockGameClass() {
        Team team = mock(Team.class);
        when(team.getScore()).thenReturn(10L);
        when(team.getProvider()).thenReturn(new RemotePlayer("http://localhost:8081"));

        Game game = mock(Game.class);
        when(game.getGameId()).thenReturn(2L);
        when(game.getTeams()).thenReturn(List.of(team));

        return game;
    }
}
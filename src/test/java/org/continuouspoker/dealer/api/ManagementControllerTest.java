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

package org.continuouspoker.dealer.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Table;
import org.continuouspoker.dealer.exceptionhandling.exceptions.ObjectNotFoundException;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.GameDAO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ManagementControllerTest {
    @Inject
    ManagementService managementService;

    @BeforeEach
    void setup() {
        managementService = mock(ManagementService.class);
    }

    @Test
    void registerPlayer() throws ObjectNotFoundException {
        Mockito.doNothing().when(managementService).registerPlayer(1L, "http://remotePlayer", "Buck Lakers");

        given().log().all()
            .auth().
                preemptive()
            .basic("admin", "admin")
            .pathParam("gameId", 1)
            .queryParam("playerUrl", "http://remotePlayer")
            .queryParam("teamName", "Team1")
            .when()
            .post("/games/manage/{gameId}/players")
            .then().log().all()
            .statusCode(204);
    }

    @Test
    void removePlayer() {
    }

    @Test
    void getPlayers() {
        Mockito.when(managementService.getPlayers(1L)).thenReturn(
            List.of("Yellow Jackets", "Brave Bulls", "Buck Lakers")
        );

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
        .when()
            .get("/games/{gameId}/players", 1L)
        .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$", isA(List.class))
            .body("$", hasSize(3))
            .body("[0]", equalTo("Yellow Jackets"))
            .body("[1]", equalTo("Brave Bulls"))
            .body("[2]", equalTo("Buck Lakers"));
    }

    @Test
    void start() {
    }

    @Test
    void getGameStatus() {
        Mockito.when(managementService.getStatus(1)).thenReturn("{\"state\":\" stopped" + "\"}");

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given().get("/games/{gameId}", 1)
            .then()
            .statusCode(200)
               .body(equalTo("{\"state\":\" stopped" + "\"}"));
    }

    @Test
    void getScore() {
        Mockito.when(managementService.getScore(1L)).thenReturn(
            Map.of("Yellow Jackets", 30L,
                "Brave Bulls", 15L));

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
        .when()
            .get("/games/{gameId}/score")
        .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("'Yellow Jackets'", equalTo(30))
            .body("'Brave Bulls'", equalTo(15));
    }

    @Test
    void listGames() {
        final GameDAO dao = Mockito.mock(GameDAO.class);
        Mockito.when(managementService.listGames()).thenReturn(
            List.of(
                new Game(1L, "game1", Duration.ZERO, Duration.ZERO, dao),
                new Game(2L, "game2", Duration.ZERO, Duration.ZERO, dao)
            )
        );

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
        .when()
            .get("/games/")
        .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body( "[0].gameId", equalTo(1))
            .body( "[0].name", equalTo("game1"))
            .body( "[1].gameId", equalTo(2))
            .body( "[1].name", equalTo("game2"));
    }

    @Test
    void delete() {
    }

    @Test
    void toggleRun() {
    }

    @Test
    void getScoreHistory() {
        ScoreHistoryEntry mockedEntry = new ScoreHistoryEntry(Instant.parse("2021-02-09T11:19:42.12Z"), 30);
        Mockito.when(managementService.getScoreHistory(1L)).thenReturn(
            Map.of("Yellow Jackets", List.of(mockedEntry)));

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
        .when()
            .get("/games/{gameId}/scoreHistory")
        .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("'Yellow Jackets'", isA(List.class))
            .body("'Yellow Jackets'[0].creationTimestamp", equalTo("2021-02-09T11:19:42.120Z"))
            .body("'Yellow Jackets'[0].score", equalTo(30));
    }

    @Test
    @Disabled
    void filterLog() throws ObjectNotFoundException {
        LogEntry mock = new LogEntry(ZonedDateTime.parse("2024-02-13T09:01:37.462115+01:00"), 1L, 0L, 5L, "");
        Mockito.when(managementService.filterLog(1L, "2024-02-13T09:00:00", "2024-02-13T21:00:00", 0L, 25, "desc"))
               .thenReturn(List.of(mock));

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        Response response = given()
            .pathParam("gameId", 1L)
            .queryParam("from", "2024-02-13T09:00:00")
            .queryParam("to", "2024-02-13T09:05:00")
            .queryParam("tableId", 0L)
            .queryParam("limit", 50)
            .queryParam("order", "desc")
        .when()
            .get("/games/{gameId}/log");

    }

    @Test
    @Disabled
    void getStateOfTournament() throws ObjectNotFoundException {
        Player mockedPlayer = new Player("Brave Bulls", Status.ACTIVE, 100, 0, (table, logger) -> 0);
        List<Player> mockedPlayers = List.of(mockedPlayer);
        Mockito.when(managementService.getStateOfTournament(1L, 0L)).thenReturn(
            new Table(0L, mockedPlayers, 5)
        );

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
            .pathParam("tournamentId", 0L)
        .when()
            .get("/games/{gameId}/tournament/{tournamentId}")
        .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$.tournamentId", equalTo(0L))
            .body("$.players[0]", hasSize(1))
            .body("$.players[0].name", equalTo("Brave Bulls"))
            .body("$.players[0].status", equalTo(Status.ACTIVE))
            .body("$.smallBlind", equalTo(5));
    }

    @Test
    @Disabled
    void getStateOfRound() throws ObjectNotFoundException {
        stubTable();
    }

    @Test
    @Disabled
    void getGameHistory() {
    }

    @Test
    void testGetLogSince() throws ObjectNotFoundException {
        Mockito.when(managementService.getLogSince(1L, "2024-02-13T09:01:37.462115+01:00")).thenReturn(
            List.of(createLogEntry())
        );

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
            .pathParam("timestamp", "2024-02-13T09:01:37.462115+01:00")
        .when()
            .get("/games/{gameId}/log/{timestamp}")
        .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$", isA(List.class))
            .body("$", hasSize(1))
            .body("[0].timestamp", equalTo("2024-02-13T09:01:37.462115+01:00"))
            .body("[0].gameId", equalTo(1))
            .body("[0].tournamentId", equalTo(0))
            .body("[0].roundId", equalTo(5));
    }

    @Test
    void getLatestTournamentAndRound() throws ObjectNotFoundException {
        when(managementService.getLatestTournamentAndRound(1L))
            .thenReturn("""
                        {
                            "tournamentId": 5
                            "roundId": 10
                        }
                        """);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .when()
            .get("/games/{gameId}/latestIds", 1L)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body(equalTo("""
                        {
                            "tournamentId": 5
                            "roundId": 10
                        }
                        """));
    }

    private void stubTable() throws ObjectNotFoundException {
        Player mockedPlayer = new Player("Brave Bulls", Status.ACTIVE, 100, 0, (table, logger) -> 0);
        List<Player> mockedPlayers = List.of(mockedPlayer);
        Mockito.when(managementService.getStateOfTournament(1L, 0L)).thenReturn(
            new Table(0L, mockedPlayers, 5)
        );
    }

    private LogEntry createLogEntry() {
        return new LogEntry(ZonedDateTime.parse("2024-02-13T09:01:37.462115+01:00"), 1L, 0L, 5L, "");
    }

    private void validateLogEntryList(Response response) {
        response.then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$", isA(List.class))
//            .body("$", hasSize(1))
            .body("[0].timestamp", equalTo("2024-02-13T09:01:37.462115+01:00"))
            .body("[0].gameId", equalTo(1))
            .body("[0].tournamentId", equalTo(0))
            .body("[0].roundId", equalTo(5));
    }
}
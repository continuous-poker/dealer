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
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.MediaType;
import org.continuouspoker.dealer.GameManager;
import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Pot;
import org.continuouspoker.dealer.data.Rank;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Suit;
import org.continuouspoker.dealer.data.Table;
import org.continuouspoker.dealer.exceptionhandling.exceptions.ObjectNotFoundException;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.GameDAO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ManagementControllerTest {
    @ConfigProperty(name = "quarkus.security.users.embedded.users.admin")
    /* package */ String adminUser;
    @ConfigProperty(name = "quarkus.security.users.embedded.users.admin")
    /* package */ String adminPassword;
    ManagementService managementService;
    GameManager gameManager;
    LogEntry logEntry;
    Map<String, String> gameDetails;
    Map<String, String> teamDetails;

    @BeforeEach
    void setup() {
        managementService = mock(ManagementService.class);
        gameManager = mock(GameManager.class);
        logEntry = new LogEntry(ZonedDateTime.parse("2024-02-13T09:02:30.462115+01:00"), 1L, 0L, 5L, "");
        gameDetails = Map.of(
            "gameId", "101",
            "name", "test");
        teamDetails = Map.of(
            "gameId", "101",
            "playerUrl", "http://remotePlayer",
            "teamName", "Yellow Jackets");
    }

    // Management methods:
    @Test
    void whenRegisterPlayer_SuccessfulRequest() throws ObjectNotFoundException {
        final long gameId = Long.parseLong(gameDetails.get("gameId"));
        final String playerUrl = teamDetails.get("playerUrl");
        final String teamName = teamDetails.get("teamName");

        Mockito.when(managementService.start(gameDetails.get("name"))).thenReturn(gameId);
        Mockito.doNothing().when(managementService).registerPlayer(gameId, playerUrl, teamName);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        authenticateAsAdmin()
            .pathParam("gameId", gameId)
            .queryParam("playerUrl", playerUrl)
            .queryParam("teamName", teamName)
            .when()
            .post("/games/manage/{gameId}/players")
            .then()
            .statusCode(204);
    }

    @Test
    void whenRegisterPlayer_FailedAuthentication() throws ObjectNotFoundException {
        final long gameId = Long.parseLong(gameDetails.get("gameId"));
        final String playerUrl = teamDetails.get("playerUrl");
        final String teamName = teamDetails.get("teamName");

        Mockito.when(managementService.start(gameDetails.get("name"))).thenReturn(gameId);
        Mockito.doNothing().when(managementService).registerPlayer(gameId, playerUrl, teamName);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", gameId)
            .queryParam("playerUrl", playerUrl)
            .queryParam("teamName", teamName)
            .when()
            .post("/games/manage/{gameId}/players")
            .then()
            .statusCode(401);
    }

    @Test
    void whenRegisterPlayer_GameNotFound_ReturnErrorStatusCode() throws ObjectNotFoundException {
        final long gameId = Long.parseLong(gameDetails.get("gameId"));
        final String playerUrl = teamDetails.get("playerUrl");
        final String teamName = teamDetails.get("teamName");

        Mockito.doThrow(ObjectNotFoundException.class).when(managementService).registerPlayer(gameId, playerUrl, teamName);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        authenticateAsAdmin()
            .pathParam("gameId", gameId)
            .queryParam("playerUrl", playerUrl)
            .queryParam("teamName", teamName)
            .when()
            .post("/games/manage/{gameId}/players")
            .then()
            .statusCode(404);
    }

    @Test
    void whenRemovePlayer_SuccessfulRequest() throws ObjectNotFoundException {
        final long gameId = Long.parseLong(teamDetails.get("gameId"));
        final String playerUrl = teamDetails.get("playerUrl");
        final String teamName = teamDetails.get("teamName");

        Mockito.when(managementService.start(gameDetails.get("name"))).thenReturn(gameId);
        Mockito.doNothing().when(managementService).registerPlayer(gameId, playerUrl, teamName);
        Mockito.doNothing().when(managementService).removePlayer(gameId, teamName);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        authenticateAsAdmin()
            .pathParam("gameId", gameId)
            .queryParam("teamName", teamName)
            .when()
            .delete("/games/manage/{gameId}/players")
            .then()
            .statusCode(204);
    }

    @Test
    void whenRemovePlayer_FailedAuthentication() throws ObjectNotFoundException {
        final long gameId = Long.parseLong(teamDetails.get("gameId"));
        final String playerUrl = teamDetails.get("playerUrl");
        final String teamName = teamDetails.get("teamName");

        Mockito.when(managementService.start(gameDetails.get("name"))).thenReturn(gameId);
        Mockito.doNothing().when(managementService).registerPlayer(gameId, playerUrl, teamName);
        Mockito.doNothing().when(managementService).removePlayer(gameId, teamName);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", gameId)
            .queryParam("teamName", teamName)
            .when()
            .delete("/games/manage/{gameId}/players")
            .then()
            .statusCode(401);
    }

    @Test
    void startGame_SuccessfulRequest() {
        long gameId = Long.parseLong(gameDetails.get("gameId"));
        String name = gameDetails.get("name");
        Mockito.when(managementService.start(name)).thenReturn(gameId);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        Response response = authenticateAsAdmin()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .formParam("name", name)
            .when()
            .post("/games/manage/");

        validateGameId(response, gameId);
    }

    @Test
    void startGame_FailedAuthentication() {
        long gameId = Long.parseLong(gameDetails.get("gameId"));
        String name = gameDetails.get("name");
        Mockito.when(managementService.start(name)).thenReturn(gameId);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .formParam("name", name)
            .when()
            .post("/games/manage/")
            .then()
            .statusCode(401);
    }

    @Test
    void whenToggleRun_SuccessfulRequest() {
        long gameId = Long.parseLong(gameDetails.get("gameId"));
        String name = gameDetails.get("name");
        Mockito.when(managementService.start(name)).thenReturn(gameId);
        Mockito.doNothing().when(managementService).toggleRun(gameId);
        Mockito.when(managementService.getStatus(gameId)).thenReturn("{\"state\":\" running" + "\"}");

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .auth()
            .preemptive()
            .basic(adminUser, adminPassword)
            .pathParam("gameId", gameId)
            .when()
            .put("/games/manage/{gameId}")
            .then()
            .statusCode(204);

        given()
            .pathParam("gameId", gameId)
            .when()
            .get("/games/{gameId}")
            .then()
            .statusCode(200)
            .body(equalTo("{\"state\":\" running" + "\"}"));
    }

    @Test
    void whenToggleRun_FailedAuthentication() {
        long gameId = Long.parseLong(gameDetails.get("gameId"));
        String name = gameDetails.get("name");
        Mockito.when(managementService.start(name)).thenReturn(gameId);
        Mockito.doNothing().when(managementService).toggleRun(gameId);
        Mockito.when(managementService.getStatus(gameId)).thenReturn("{\"state\":\" running" + "\"}");

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", gameId)
            .when()
            .put("/games/manage/{gameId}")
            .then()
            .statusCode(401);
    }

    @Test
    void whenDeleteGame_SuccessfulRequest() {
        long gameId = Long.parseLong(gameDetails.get("gameId"));
        Mockito.when(managementService.start(gameDetails.get("name"))).thenReturn(gameId);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        Response response = publishGameName(authenticateAsAdmin())
            .post("/games/manage/");
        validateGameId(response, gameId);

        Mockito.doNothing().when(managementService).delete(gameId);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        authenticateAsAdmin()
            .pathParam("gameId", gameId)
            .when()
            .delete("/games/manage/{gameId}")
            .then()
            .statusCode(204);
    }

    @Test
    void whenDeleteGame_FailedAuthentication() {
        long gameId = Long.parseLong(gameDetails.get("gameId"));
        Mockito.doNothing().when(managementService).delete(gameId);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", gameId)
            .when()
            .delete("/games/manage/{gameId}")
            .then()
            .statusCode(401);
    }

    // Game
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
    void getGameStatus() {
        Mockito.when(managementService.getStatus(1)).thenReturn("{\"state\":\" stopped" + "\"}");

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given().get("/games/{gameId}", 1)
               .then()
               .statusCode(200)
               .body(equalTo("{\"state\":\" stopped" + "\"}"));
    }

    @Test
    void getGameHistory() throws ObjectNotFoundException {
        List<String> logs = List.of("09:16:15.422065+01:00 Starting round 10.");
        Mockito.when(managementService.getGameHistory(1L)).thenReturn(
            Map.of(0L, Map.of(
                3L, logs)));

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
            .when()
            .get("/games/{gameId}/history")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("0", isA(Map.class))
            .body("0.3", isA(List.class))
            .body("0.3", equalTo(logs));
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

    // Only covers a positive case which corresponds to the first game created in the database ever.

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
    void getStateOfTournament() throws ObjectNotFoundException {
        Table table = createTableInstance();
        Mockito.when(managementService.getStateOfTournament(1L, 0L)).thenReturn(table);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        Response response = given()
            .pathParam("gameId", 1L)
            .pathParam("tournamentId", 0L)
            .when()
            .get("/games/{gameId}/tournament/{tournamentId}");

        validateTable(response);
    }

    @Test
    void getStateOfRound() throws ObjectNotFoundException {
        Table table = createTableInstance();
        Mockito.when(managementService.getStateOfRound(1L, 0L, 5L)).thenReturn(table);

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        Response response = given()
            .pathParam("gameId", 1L)
            .pathParam("tournamentId", 0L)
            .pathParam("roundId", 5L)
            .when()
            .get("/games/{gameId}/tournament/{tournamentId}/round/{roundId}");

        validateTable(response);
    }

    // Game logs
    @Test
    void filterLog() throws ObjectNotFoundException {
        Mockito.when(managementService.filterLog(1L, "2024-02-13T09:00:00", "2024-02-13T09:10:00", 0L, 25, "desc"))
               .thenReturn(List.of(logEntry));

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
            .queryParam("from", "2024-02-13T09:00:00")
            .queryParam("to", "2024-02-13T09:10:00")
            .queryParam("tableId", 0L)
            .queryParam("limit", 25)
            .queryParam("order", "desc")
            .when()
            .get("/games/{gameId}/log")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$", isA(List.class))
            .body("$", hasSize(1))
            .body("[0].timestamp", equalTo("2024-02-13T09:02:30.462115+01:00"))
            .body("[0].gameId", equalTo(1))
            .body("[0].tournamentId", equalTo(0))
            .body("[0].roundId", equalTo(5));

    }

    @Test
    void testGetLogSince() throws ObjectNotFoundException {
        Mockito.when(managementService.getLogSince(1L, "2024-02-13T09:00:00")).thenReturn(
            List.of(logEntry)
        );

        QuarkusMock.installMockForType(managementService, ManagementService.class);

        given()
            .pathParam("gameId", 1L)
            .pathParam("timestamp", "2024-02-13T09:00:00")
            .when()
            .get("/games/{gameId}/log/{timestamp}")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$", isA(List.class))
            .body("$", hasSize(1))
            .body("[0].timestamp", equalTo(logEntry.getTimestamp().toString()))
            .body("[0].gameId", equalTo(1))
            .body("[0].tournamentId", equalTo(0))
            .body("[0].roundId", equalTo(5));
    }

    private RequestSpecification authenticateAsAdmin() {
        return given()
            .auth()
            .preemptive()
            .basic(adminUser, adminPassword);
    }

    private RequestSpecification publishGameName(RequestSpecification requestSpecification) {
        return requestSpecification
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .formParam("name", "test");
    }

    private void validateGameId(Response response, Long gameId) {
        response
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body(equalTo(String.valueOf(gameId)));
    }

    private Table createTableInstance() {
        List<Card> cards = List.of(
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.KING, Suit.DIAMONDS)
        );
        List<Player> players = List.of(
            new Player("Brave Bulls", Status.ACTIVE, 100, 30, (table, logger) -> 0),
            new Player("Yellow Jackets", Status.ACTIVE, 150, 40, (table, logger) -> 0)
        );

        Table table = new Table(0L, players, 15);
        table.setMinimumBet(300);
        table.setPot(new Pot(System.out::println));
        table.setActivePlayer(players.get(1));

        cards.forEach(table::takeCard);

        return table;
    }

    private void validateTable(Response response) {
        response.then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("activePlayer", equalTo(1))
                .body("minimumBet", equalTo(300))
                .body("smallBlind", equalTo(15));
    }
}
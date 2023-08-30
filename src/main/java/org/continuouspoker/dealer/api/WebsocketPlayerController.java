package org.continuouspoker.dealer.api;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import lombok.RequiredArgsConstructor;
import org.continuouspoker.dealer.exceptionhandling.exceptions.ObjectNotFoundException;

@ServerEndpoint("/websocket/{gameId}/{teamName}")
@RequiredArgsConstructor
@ApplicationScoped
public class WebsocketPlayerController {


    public static final String PARAM_GAME_ID = "gameId";
    public static final String PARAM_TEAM_NAME = "teamName";
    private final ManagementService service;

    @OnOpen
    public void onOpen(Session session, @PathParam(PARAM_GAME_ID) long gameId, @PathParam(PARAM_TEAM_NAME) String teamName)
            throws ObjectNotFoundException {
        service.registerPlayer(gameId, session, teamName);
    }

    @OnClose
    public void onClose(Session session, @PathParam(PARAM_GAME_ID) long gameId, @PathParam(PARAM_TEAM_NAME) String teamName) {
        service.handleWebsocketDisconnect(gameId, session, teamName);
    }

    @OnError
    public void onError(Session session, @PathParam(PARAM_GAME_ID) long gameId, @PathParam(PARAM_TEAM_NAME) String teamName, Throwable throwable) {

    }

    @OnMessage
    public void onMessage(Session session, @PathParam(PARAM_GAME_ID) long gameId, @PathParam(PARAM_TEAM_NAME) String teamName, String message) {
        service.handleWebsocketMessage(gameId, session, teamName, message);
    }
}

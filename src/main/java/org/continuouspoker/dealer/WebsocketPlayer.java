package org.continuouspoker.dealer;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.continuouspoker.dealer.data.Table;

@RequiredArgsConstructor
@Slf4j
public class WebsocketPlayer implements ActionProvider {

    private static final int QUEUE_LENGTH = 3;

    @Setter
    @Getter
    private Session session;

    @Getter
    private BlockingQueue<String> messages = new ArrayBlockingQueue<String>(QUEUE_LENGTH);

    @Getter
    @Setter
    private boolean active;

    @JsonProperty
    private int strike;
    private long blockedTable = -1;

    public WebsocketPlayer(final Session session) {
        this.session = session;
        this.active = true;
        log.debug("Websocket Action Provider created for Websocket session {}", session.getId());
    }

    @Override
    public int requestBet(final Table table) {
        if (table.getTournamentId() == blockedTable || !active) {
            return 0;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.session.getBasicRemote().sendText(objectMapper.writeValueAsString(table));
        } catch (IOException e) {
            log.error("Error while requesting bet from websocket player with id {}", session.getId(), e);
            addStrike(table);
        }

        while (table.getTournamentId() != blockedTable) {
            Optional<String> message = pollMessage();
            if (message.isEmpty()) {
                addStrike(table);
                continue;
            }

            if (!StringUtils.isNumeric(message.get())) {
                log.error("Response not numeric");
                addStrike(table);
                continue;
            }

            int bet = Integer.parseInt(message.get());

            return bet;
        }

        return 0;
    }

    private Optional<String> pollMessage() {
        try {
            return Optional.ofNullable(messages.poll(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            log.error("Interrupted while polling message {}", session.getId(), e);
            return Optional.empty();
        }
    }

    private void addStrike(final Table table) {
        strike++;
        if (strike == MAX_STRIKES) {
            blockedTable = table.getTournamentId();
            strike = 0;
        }
    }
}

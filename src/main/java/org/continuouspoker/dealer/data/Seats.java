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
package org.continuouspoker.dealer.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

public class Seats implements Serializable {

    private final List<Player> players;

    private int position;
    @Getter
    private int lastBet;
    @Getter
    private Player lastBettingPlayer;

    public Seats(final List<Player> players) {
        this.players = new LinkedList<>(players);
    }

    public Player getCurrentPlayer() {
        return players.get(position);
    }

    public Player getNextPlayer() {
        position++;
        if (position == players.size()) {
            position = 0;
        }
        return players.get(position);
    }

    public Player getNextActivePlayer() {
        final Player initialPlayer = getCurrentPlayer();

        if (allPlayersInactive()) {
            throw new IllegalStateException();
        }

        Player nextPlayer = getNextPlayer();
        while (!nextPlayer.getStatus().equals(Status.ACTIVE)) {
            nextPlayer = getNextPlayer();
        }
        if (nextPlayer == initialPlayer) {
            return null;
        }
        return nextPlayer;
    }

    private boolean allPlayersInactive() {
        return players.stream().map(Player::getStatus).allMatch(Status.OUT::equals);
    }

    public int getNumberOfActivePlayers() {
        return (int) players.stream().map(Player::getStatus).filter(Status.ACTIVE::equals).count();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setLastBetToCurrentPlayer() {
        lastBet = getCurrentPlayer().getCurrentBet();
        lastBettingPlayer = getCurrentPlayer();
    }
}

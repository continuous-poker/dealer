/*
 * Copyright © 2024 DoubleSlash Net-Business GmbH
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.continuouspoker.dealer.calculation.hands.Score;

@Getter
@JsonPropertyOrder(alphabetic = true)
public class Table implements CardReceiver, Serializable {

    @JsonIgnore
    private final long tournamentId;
    private final List<Card> communityCards = new ArrayList<>();
    private final List<Player> players;

    @Getter
    private int round = 1;
    private int smallBlind;

    @Setter
    private int minimumBet;
    @JsonIgnore
    @Setter
    private Pot pot;
    private int activePlayer;
    private int currentDealer;


    public Table(final long tournamentId, final List<Player> players, final int smallBlind) {
        this.tournamentId = tournamentId;
        this.players = players;
        this.smallBlind = smallBlind;
        this.minimumBet = smallBlind * 2;
    }

    @JsonProperty("pot")
    public int getPotSize() {
        return pot.getTotalSize();
    }

    @JsonProperty
    public int getMinimumRaise() {
        return minimumBet * 2;
    }

    public void setActivePlayer(final Player player) {
        this.activePlayer = players.indexOf(player);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public void moveDealerButton() {
        final Player dealer = players.get(currentDealer);
        final Player newDealer = getNextActivePlayer(dealer);
        currentDealer = players.indexOf(newDealer);
    }

    private Player getNextActivePlayer(final Player player) {
        int index = players.indexOf(player) + 1;
        if (index == players.size()) {
            index = 0;
        }
        final Player nextPlayer = players.get(index);
        if (nextPlayer.getStatus().equals(Status.OUT)) {
            return getNextActivePlayer(nextPlayer);
        } else {
            return nextPlayer;
        }
    }

    @JsonIgnore
    public List<Player> getPlayersInPlayOrder() {
        final List<Player> playersInOrder = new ArrayList<>();
        Player player = players.get(getCurrentDealer());
        for (int i = 0; i < getNumberOfActivePlayers(); i++) {
            player = getNextActivePlayer(player);
            playersInOrder.add(player);
        }
        return playersInOrder;
    }

    private int getNumberOfActivePlayers() {
        return (int) players.stream().filter(p -> !p.getStatus().equals(Status.OUT)).count();
    }

    @Override
    public void takeCard(final Card card) {
        communityCards.add(card);
    }

    public void collectChips(final List<Player> playersInPlayOrder) {
        pot.collect(playersInPlayOrder);
    }

    public void payWinner(final Player winner) {
        pot.pay(winner);
    }

    public void payWinners(final Map<Score, List<Player>> rankedPlayers) {
        pot.pay(rankedPlayers);
    }

    public void resetForNextRound() {
        communityCards.clear();
        moveDealerButton();

        round++;

        if (hasDealerButtonCycledTwice()) {
            smallBlind *= 2;
        }

        minimumBet = smallBlind * 2;
    }

    private boolean hasDealerButtonCycledTwice() {
        return round % players.size() * 2 == 0;
    }

    public Table copyForActivePlayer() {
        final Table clonedTable = SerializationUtils.clone(this);
        final Player activePlayerObject = clonedTable.getPlayers().get(clonedTable.getActivePlayer());

        final List<Player> players2 = clonedTable.getPlayers();
        for (final Player p : players2) {
            if (!p.equals(activePlayerObject)) {
                p.clearCards();
            }
        }
        return clonedTable;
    }

}

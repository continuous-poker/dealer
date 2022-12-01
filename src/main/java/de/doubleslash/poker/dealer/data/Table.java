package de.doubleslash.poker.dealer.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@AllArgsConstructor
public class Table implements CardReceiver, Serializable {

    private final List<Card> communityCards;
    private final List<Player> players;

    @Getter     //Added for LogSave
    private int round;
    private int smallBlind;
    private int minimumBet;
    @JsonIgnore
    private final Pot pot;
    private int activePlayer;
    private int currentDealer;

    @JsonIgnore
    private final long id;

    public Table(final long id, final List<Player> players, final int smallBlind, final Consumer<String> tableLogger) {
        this(new ArrayList<>(), players, 1, smallBlind, smallBlind * 2, new Pot(tableLogger), 0, 0, id);
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
        Player p = players.get(getCurrentDealer());
        for (int i = 0; i < getNumberOfActivePlayers(); i++) {
            p = getNextActivePlayer(p);
            playersInOrder.add(p);
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

    public void payWinners(final Map<int[], List<Player>> rankedPlayers) {
        pot.pay(rankedPlayers);
    }

    public void setMinimumBet(final int bet) {
        minimumBet = bet;
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
        final Table t = SerializationUtils.clone(this);
        final Player activePlayerObject = t.getPlayers().get(t.getActivePlayer());

        final List<Player> players2 = t.getPlayers();
        for (final Player p : players2) {
            if (!p.equals(activePlayerObject)) {
                p.clearCards();
            }
        }
        return t;
    }

}

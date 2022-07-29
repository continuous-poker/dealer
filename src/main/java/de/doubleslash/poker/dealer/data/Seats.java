package de.doubleslash.poker.dealer.data;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

public class Seats {

    private final LinkedList<Player> players;

    private int position = 0;
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
        if (allPlayersInactive()) {
            throw new IllegalStateException();
        }

        Player nextPlayer = getNextPlayer();
        while (!nextPlayer.getStatus().equals(Status.ACTIVE)) {
            nextPlayer = getNextPlayer();
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
        lastBet = getCurrentPlayer().getBet();
        lastBettingPlayer = getCurrentPlayer();
    }
}

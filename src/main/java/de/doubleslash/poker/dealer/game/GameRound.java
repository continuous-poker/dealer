package de.doubleslash.poker.dealer.game;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.calculation.HandCalculator;
import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Deck;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Status;
import de.doubleslash.poker.dealer.data.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GameRound {

    private final List<Player> players;
    private final Table table;
    private final GameLogger history;
    private final long gameId;

    private final Duration timeBetweenSteps;

    public Table run() {
        return playWithDeck(new Deck());
    }

    protected Table playWithDeck(final Deck deck) {

        final List<Player> playersInPlayOrder = table.getPlayersInPlayOrder();
        deck.dealCards(playersInPlayOrder, 2);
        deck.burnCard();

        history.log(gameId, table.getId(), table.getRound(), "Starting round %s.", table.getRound());

        try {
            if (determineWinner(table, playersInPlayOrder, true)) {
                return table;
            }

            sleep();

            deal(table, deck, 3);
            logFlop(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return table;
            }

            sleep();

            deal(table, deck, 1);
            logTurn(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return table;
            }

            sleep();

            deal(table, deck, 1);
            logRiver(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return table;
            }

            sleep();

            showdown(table, playersInPlayOrder);
            return table;

        } finally {
            history.log(gameId, table.getId(), table.getRound(), "Ending round %s.", table.getRound());

            checkPlayerState(playersInPlayOrder);
            clearCards(players);
            table.resetForNextRound();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(timeBetweenSteps.toMillis());
        } catch (InterruptedException e) {
            log.error("Got interrupted in sleep", e);
            Thread.currentThread().interrupt();
        }
    }

    private void logFlop(final Table table) {
        final String dealtCards = table.getCommunityCards()
                                       .stream()
                                       .map(Card::toString)
                                       .collect(Collectors.joining(", "));
        history.log(gameId, table.getId(), table.getRound(), "Flop: %s", dealtCards);
    }

    private void logTurn(final Table table) {
        final String dealtCards = table.getCommunityCards()
                                       .stream()
                                       .skip(3)
                                       .map(Card::toString)
                                       .collect(Collectors.joining());
        history.log(gameId, table.getId(), table.getRound(), "Turn: %s", dealtCards);
    }

    private void logRiver(final Table table) {
        final String dealtCards = table.getCommunityCards()
                                       .stream()
                                       .skip(4)
                                       .map(Card::toString)
                                       .collect(Collectors.joining());
        history.log(gameId, table.getId(), table.getRound(), "River: %s", dealtCards);
    }

    private void checkPlayerState(final List<Player> playersInPlayOrder) {
        for (final Player player : playersInPlayOrder) {
            if (player.getStack() == 0) {
                player.out();
            } else {
                player.active();
            }
        }
    }

    private boolean determineWinner(final Table table, final List<Player> playersInPlayOrder, final boolean isPreFlop) {
        if (!everyoneIsAllIn(playersInPlayOrder)) {
            final Optional<Player> winningPlayer = new BetRound(gameId, table, playersInPlayOrder, isPreFlop, history).run();
            table.collectChips(playersInPlayOrder);
            if (winningPlayer.isPresent()) {
                final Player winner = winningPlayer.get();
                table.payWinner(winner);
                return true;
            }
        }
        return false;
    }

    private void showdown(final Table table, final List<Player> players) {
        final List<Player> playersStillActive = players.stream().filter(p -> p.getStatus() == Status.ACTIVE).toList();

        final Map<int[], List<Player>> rankedPlayers = new HandCalculator().determineWinningHand(playersStillActive,
                Collections.unmodifiableList(table.getCommunityCards()));

        rankedPlayers.values().stream().flatMap(Collection::stream).forEach(player -> logPlayerCards(table, player));

        table.payWinners(rankedPlayers);
    }

    private void logPlayerCards(final Table table, final Player player) {
        history.log(gameId, table.getId(), table.getRound(), "Player %s has %s.", player.getName(),
                player.getCards().stream().map(Card::toString).collect(Collectors.joining(" and ")));
    }

    private boolean everyoneIsAllIn(final List<Player> playersInPlayOrder) {
        final long activePlayerCount = getActivePlayerStream(playersInPlayOrder).count();
        final long allInCount = getActivePlayerStream(playersInPlayOrder).filter(Player::isAllIn).count();

        return allInCount >= activePlayerCount - 1;

    }

    private Stream<Player> getActivePlayerStream(final List<Player> playersInPlayOrder) {
        return playersInPlayOrder.stream().filter(p -> p.getStatus().equals(Status.ACTIVE));
    }

    private void deal(final Table table, final Deck deck, final int cards) {
        deck.dealCards(table, cards);
        deck.burnCard();
    }

    private void clearCards(final List<Player> players) {
        players.forEach(p -> p.getCards().clear());
    }
}

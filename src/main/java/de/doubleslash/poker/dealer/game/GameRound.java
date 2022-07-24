package de.doubleslash.poker.dealer.game;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    private final GameLogger logger;
    private final long gameId;

    public void run() {
        playWithDeck(new Deck());
    }

    protected void playWithDeck(final Deck deck) {
        logger.log(gameId, table.getId(), "Starting round %s.", table.getRound());

        final List<Player> playersInPlayOrder = table.getPlayersInPlayOrder();
        deck.dealCards(playersInPlayOrder, 2);
        deck.burnCard();

        try {
            if (determineWinner(table, playersInPlayOrder, true)) {
                return;
            }

            sleep(500, TimeUnit.MILLISECONDS);

            deal(table, deck, 3);
            logFlop(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return;
            }

            sleep(500, TimeUnit.MILLISECONDS);

            deal(table, deck, 1);
            logTurn(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return;
            }

            sleep(500, TimeUnit.MILLISECONDS);

            deal(table, deck, 1);
            logRiver(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return;
            }

            sleep(500, TimeUnit.MILLISECONDS);

            showdown(table, playersInPlayOrder);

        } finally {

            checkPlayerState(playersInPlayOrder);
            clearCards(players);
            logger.log(gameId, table.getId(), "Ending round %s.", table.getRound());
            table.resetForNextRound();

        }
    }

    private void sleep(final int sleeptime, final TimeUnit unit) {
        try {
            Thread.sleep(unit.toMillis(sleeptime));
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
        logger.log(gameId, table.getId(), "Flop: %s", dealtCards);
    }

    private void logTurn(final Table table) {
        final String dealtCards = table.getCommunityCards()
                                       .stream()
                                       .skip(3)
                                       .map(Card::toString)
                                       .collect(Collectors.joining());
        logger.log(gameId, table.getId(), "Turn: %s", dealtCards);
    }

    private void logRiver(final Table table) {
        final String dealtCards = table.getCommunityCards()
                                       .stream()
                                       .skip(4)
                                       .map(Card::toString)
                                       .collect(Collectors.joining());
        logger.log(gameId, table.getId(), "River: %s", dealtCards);
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
            final Optional<Player> winningPlayer = new BetRound(gameId, table, playersInPlayOrder, isPreFlop,
                    logger).run();
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

        table.payWinners(rankedPlayers);
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

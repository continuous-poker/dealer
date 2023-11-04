package org.continuouspoker.dealer.game;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.continuouspoker.dealer.GameRoundLogEntry;
import org.continuouspoker.dealer.calculation.HandCalculator;
import org.continuouspoker.dealer.calculation.hands.Score;
import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Deck;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Pot;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Table;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GameRound {

    private static final int NUMBER_OF_FLOP_CARDS = 3;
    private static final int NUMBER_OF_TURN_CARDS = 1;
    private static final int NUMBER_OF_RIVER_CARDS = 1;

    @Getter
    private final long roundId;

    private final List<Player> players;

    @Getter
    private Table table;

    private final Duration timeBetweenSteps;

    private final List<GameRoundLogEntry> gamelog = new ArrayList<>();

    public void run() {
        playWithDeck(new Deck());
    }

    private void logStep(final String msg, final Object... values) {
        gamelog.add(new GameRoundLogEntry(roundId, ZonedDateTime.now(), String.format(msg, values)));
    }

    protected void playWithDeck(final Deck deck) {
        table.setPot(new Pot(this::logStep));

        final List<Player> playersInPlayOrder = table.getPlayersInPlayOrder();
        deck.dealCards(playersInPlayOrder, 2);
        deck.burnCard();

        logStep("Starting round %s.", table.getRound());

        try {
            if (determineWinner(table, playersInPlayOrder, true)) {
                return;
            }

            sleep();

            deal(table, deck, NUMBER_OF_FLOP_CARDS);
            logFlop(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return;
            }

            sleep();

            deal(table, deck, NUMBER_OF_TURN_CARDS);
            logTurn(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return;
            }

            sleep();

            deal(table, deck, NUMBER_OF_RIVER_CARDS);
            logRiver(table);

            if (determineWinner(table, playersInPlayOrder, false)) {
                return;
            }

            sleep();

            showdown(table, playersInPlayOrder);

        } finally {
            logStep("Ending round %s.", table.getRound());

            checkPlayerState(playersInPlayOrder, false);
            final var clonedTable = SerializationUtils.clone(table);
            checkPlayerState(playersInPlayOrder, true);

            clearCards(players);
            table.resetForNextRound();

            // store last table state for history
            this.table = clonedTable;
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
        logStep("Flop: %s", dealtCards);
    }

    private void logTurn(final Table table) {
        final String dealtCards = table.getCommunityCards()
                                       .stream()
                                       .skip(NUMBER_OF_FLOP_CARDS)
                                       .map(Card::toString)
                                       .collect(Collectors.joining());
        logStep("Turn: %s", dealtCards);
    }

    private void logRiver(final Table table) {
        final String dealtCards = table.getCommunityCards()
                                       .stream()
                                       .skip(NUMBER_OF_FLOP_CARDS + (long) NUMBER_OF_TURN_CARDS)
                                       .map(Card::toString)
                                       .collect(Collectors.joining());
        logStep("River: %s", dealtCards);
    }

    private void checkPlayerState(final List<Player> playersInPlayOrder, final boolean defaultToActive) {
        for (final Player player : playersInPlayOrder) {
            if (player.getStack() == 0) {
                player.out();
            } else if (defaultToActive) {
                player.active();
            }
        }
    }

    private boolean determineWinner(final Table table, final List<Player> playersInPlayOrder, final boolean isPreFlop) {
        if (!everyoneIsAllIn(playersInPlayOrder)) {
            final Optional<Player> winningPlayer = new BetRound(table, playersInPlayOrder, isPreFlop,
                    this::logStep).run();
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

        final Map<Score, List<Player>> rankedPlayers = new HandCalculator().determineWinningHand(playersStillActive,
                Collections.unmodifiableList(table.getCommunityCards()));

        rankedPlayers.values().stream().flatMap(Collection::stream).forEach(this::logPlayerCards);

        table.payWinners(rankedPlayers);
    }

    private void logPlayerCards(final Player player) {
        logStep("Player %s has %s.", player.getName(),
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

    public Stream<GameRoundLogEntry> getHistory() {
        return this.gamelog.stream();
    }
}

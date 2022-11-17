package de.doubleslash.poker.dealer.game;

import java.util.List;
import java.util.Optional;

import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Seats;
import de.doubleslash.poker.dealer.data.Table;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BetRound {

    private final Table table;
    private final List<Player> playersInPlayOrder;
    private final boolean isPreFlop;
    private final GameLogger logger;
    private final long gameId;

    public BetRound(final long gameId, final Table table, final List<Player> playersInPlayOrder,
            final boolean isPreFlop, final GameLogger logger) {
        this.gameId = gameId;
        this.table = table;
        this.playersInPlayOrder = playersInPlayOrder;
        this.isPreFlop = isPreFlop;
        this.logger = logger;
    }

    public Optional<Player> run() {
        logger.log(gameId, table.getId(), "Starting bet round.", table);
        final Seats seats = new Seats(playersInPlayOrder);
        if (isPreFlop) {
            collectBlinds(table, seats);
        }
        seats.setLastBetToCurrentPlayer();
        table.setMinimumBet(table.getSmallBlind() * 2);

        while (seats.getNextActivePlayer() != null) {
            table.setActivePlayer(seats.getCurrentPlayer());
            // check for only one left -> he wins
            if (seats.getNumberOfActivePlayers() == 1) {
                // we have a winner
                logger.log(gameId, table.getId(), "Ending bet round with winner: %s", table,
                        seats.getCurrentPlayer().getName());

                return Optional.of(seats.getCurrentPlayer());
            }
            if (handleCurrentPlayer(seats)) {
                break;
            }
        }

        logger.log(gameId, table.getId(), "Ending bet round.", table);
        return Optional.empty();

    }

    private boolean handleCurrentPlayer(final Seats seats) {
        final Player currentPlayer = seats.getCurrentPlayer();

        if (currentPlayer.isAllIn()) {
            return currentPlayer.equals(seats.getLastBettingPlayer());
        }

        if (currentPlayer.equals(seats.getLastBettingPlayer())) {
            return handleLastBettingPlayer(seats, currentPlayer);
        } else {
            callPlayer(table, currentPlayer);
            if (currentPlayer.getBet() > seats.getLastBet()) {
                table.setMinimumBet(currentPlayer.getBet());
                seats.setLastBetToCurrentPlayer();
            }
        }
        return false;
    }

    private boolean handleLastBettingPlayer(final Seats seats, final Player currentPlayer) {
        if (seats.getLastBet() == 0 || (isPreFlop && seats.getLastBet() == table.getSmallBlind() * 2)) {
            // nobody bet / raised, and we are at the starting player again
            // let him bet / raise or check
            final Action action = callPlayer(table, currentPlayer);
            if (action.equals(Action.CHECK)) {
                return true;
            } else if (action.equals(Action.BET) || action.equals(Action.RAISE)) {
                // he bet / raised
                seats.setLastBetToCurrentPlayer();
                if (seats.getLastBet() > 0) {
                    table.setMinimumBet(seats.getLastBet());
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private void collectBlinds(final Table table, final Seats seats) {
        handleSmallBlind(table, seats);
        handleBigBlind(table, seats);
    }

    private void handleBigBlind(final Table table, final Seats seats) {
        final Player big = seats.getNextActivePlayer();
        big.bet(table.getSmallBlind() * 2);

        if (big.isAllIn()) {
            logger.log(gameId, table.getId(), "Player %s goes all in for big blind with %s.", table, big.getName(),
                    big.getBet());
        } else {
            logger.log(gameId, table.getId(), "Player %s pays big blind of %s.", table, big.getName(), big.getBet());
            log.info("{} pays big blind of {}", big.getName(), big.getBet());
        }
    }

    private void handleSmallBlind(final Table table, final Seats seats) {
        final Player small = seats.getCurrentPlayer();
        small.bet(table.getSmallBlind());

        if (small.isAllIn()) {
            logger.log(gameId, table.getId(), "Player %s goes all in for small blind with %s.", table, small.getName(),
                    small.getBet());
        } else {
            logger.log(gameId, table.getId(), "Player %s pays small blind of %s.", table, small.getName(), small.getBet());
            log.info("{} pays small blind of {}", small.getName(), small.getBet());
        }
    }

    private Action callPlayer(final Table table, final Player player) {
        log.debug("Calling player {} with table {}", player.getName(), table);
        int result = player.getActionProvider().requestBet(table.copyForActivePlayer());
        log.debug("Player {} returned bet of {}", player.getName(), result);

        if (result < player.getBet() || result < table.getMinimumBet()) {
            log.debug("Result {} is lower than current bet of {} for player {}, setting it to the current bet.", result,
                    player.getBet(), player.getName());
            result = player.getBet();
        }

        return new BetDecision(gameId, logger).performAction(table, player, result);
    }

}

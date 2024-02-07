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

package org.continuouspoker.dealer.game;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.StepLogger;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Seats;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Table;

@Slf4j
public class BetRound {

    private final Table table;
    private final List<Player> playersInPlayOrder;
    private final boolean isPreFlop;
    private final StepLogger logger;

    public BetRound(final Table table, final List<Player> playersInPlayOrder, final boolean isPreFlop,
            final StepLogger logger) {
        this.table = table;
        this.playersInPlayOrder = playersInPlayOrder;
        this.isPreFlop = isPreFlop;
        this.logger = logger;
    }

    public Optional<Player> run() {
        logger.log("Starting bet round.");
        final Seats seats = new Seats(playersInPlayOrder);
        if (isPreFlop) {
            collectBlinds(table, seats);
        }
        seats.setLastBetToCurrentPlayer();
        table.setMinimumBet(table.getSmallBlind() * 2);

        while (seats.getNextPlayer() != null) {
            if (seats.getCurrentPlayer().getStatus().equals(Status.ACTIVE)) {
                table.setActivePlayer(seats.getCurrentPlayer());
                // check for only one left -> he wins
                if (onlyOneActivePlayerLeft(seats)) {
                    // we have a winner
                    logger.log("Ending bet round with winner: %s", seats.getCurrentPlayer().getName());

                    return Optional.of(seats.getCurrentPlayer());
                }
                if (handleCurrentPlayer(seats)) {
                    break;
                }
            } else if (seats.getLastBettingPlayer() == seats.getCurrentPlayer()) {
                // everybody checked, end the round
                break;
            }
        }

        logger.log("Ending bet round.");
        return Optional.empty();

    }

    private static boolean onlyOneActivePlayerLeft(final Seats seats) {
        return seats.getNumberOfActivePlayers() == 1;
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
            if (currentPlayer.getCurrentBet() > seats.getLastBet()) {
                table.setMinimumBet(currentPlayer.getCurrentBet());
                seats.setLastBetToCurrentPlayer();
            }
        }
        return false;
    }

    private boolean handleLastBettingPlayer(final Seats seats, final Player currentPlayer) {
        if (seats.getLastBet() == 0 || isPreFlop && seats.getLastBet() == table.getSmallBlind() * 2
                && seats.getLastBettingPlayer() != currentPlayer) {
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
            logger.log("Player %s goes all in for big blind with %s.", big.getName(), big.getCurrentBet());
        } else {
            logger.log("Player %s pays big blind of %s.", big.getName(), big.getCurrentBet());
            log.info("{} pays big blind of {}", big.getName(), big.getCurrentBet());
        }
    }

    private void handleSmallBlind(final Table table, final Seats seats) {
        final Player small = seats.getCurrentPlayer();
        small.bet(table.getSmallBlind());

        if (small.isAllIn()) {
            logger.log("Player %s goes all in for small blind with %s.", small.getName(), small.getCurrentBet());
        } else {
            logger.log("Player %s pays small blind of %s.", small.getName(), small.getCurrentBet());
            log.info("{} pays small blind of {}", small.getName(), small.getCurrentBet());
        }
    }

    private Action callPlayer(final Table table, final Player player) {
        log.debug("Calling player {} with table {}", player.getName(), table);
        int result = player.getActionProvider().requestBet(table.copyForActivePlayer(), logger);
        log.debug("Player {} returned bet of {}", player.getName(), result);

        if (result < player.getCurrentBet()) {
            log.debug("Result {} is lower than current bet of {} for player {}, setting it to the current bet.", result,
                    player.getCurrentBet(), player.getName());
            result = player.getCurrentBet();
        } else if (result < table.getMinimumBet()) {
            log.debug(
                    "Result {} is higher than current bet of {} for player {}, but lower than the minimum bet of {}, resetting it to the current bet.",
                    result, player.getCurrentBet(), player.getName(), table.getMinimumBet());
        }

        return new BetDecision(logger).performAction(table, player, result);
    }

}

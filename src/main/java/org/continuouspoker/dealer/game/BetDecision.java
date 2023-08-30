package org.continuouspoker.dealer.game;

import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.StepLogger;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Table;

@Slf4j
public class BetDecision {
    private static final String ALL_IN_TEXT = "Player %s goes all in with %s.";

    private final StepLogger logger;

    /* package */ BetDecision(final StepLogger logger) {
        this.logger = logger;
    }

    /* package */ Action performAction(final Table table, final Player player, final int bet) {
        if (noPlayerHasBetYet(table)) {
            if (bet >= table.getMinimumBet()) {
                return bet(player, bet);
            } else {
                return check(player);
            }
        } else {
            if (bet >= table.getMinimumRaise()) {
                return raise(player, bet);
            } else if (bet >= table.getMinimumBet()) {
                if (bet == player.getCurrentBet()) {
                    return check(player);
                } else {
                    return call(table.getMinimumBet(), player);
                }
            } else {
                return fold(player);
            }
        }
    }

    private boolean playerCanPayIt(final Player player, final int bet) {
        if (player.isGoingAllIn(bet)) {
            player.bet(bet);
            logger.log(ALL_IN_TEXT, player.getName(), player.getCurrentBet());
            return false;
        }
        return true;
    }

    private Action raise(final Player player, final int bet) {
        if (playerCanPayIt(player, bet)) {
            logger.log("Player %s raises to %s.", player.getName(), bet);
            player.bet(bet);
        }
        return Action.RAISE;
    }

    private Action call(final int minimumBet, final Player player) {
        if (playerCanPayIt(player, minimumBet)) {
            logger.log("Player %s calls the bet of %s.", player.getName(), minimumBet);
            player.bet(minimumBet);
        }
        return Action.CALL;
    }

    private Action bet(final Player player, final int bet) {
        if (playerCanPayIt(player, bet)) {
            logger.log("Player %s bets %s.", player.getName(), bet);
            player.bet(bet);
        }
        return Action.BET;
    }

    private Action fold(final Player player) {
        logger.log("Player %s folds.", player.getName());
        player.fold();
        return Action.FOLD;
    }

    private Action check(final Player player) {
        logger.log("Player %s checks.", player.getName());
        return Action.CHECK;
    }

    private boolean noPlayerHasBetYet(final Table table) {
        return table.getPlayers().stream().mapToInt(Player::getCurrentBet).sum() == 0;
    }

}

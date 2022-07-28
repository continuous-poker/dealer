package de.doubleslash.poker.dealer.game;

import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Table;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BetDecision {
    private static final String ALL_IN_TEXT = "Player %s goes all in with %s.";

    private final long gameId;
    private final GameLogger logger;

    BetDecision(final long gameId, final GameLogger logger) {
        this.gameId = gameId;
        this.logger = logger;
    }

    void determineAction(final Table table, final Player player, final int bet) {
        if (noPlayerHasBetYet(table)) {
            if (bet >= table.getMinimumBet()) {
                bet(table, player, bet);
            } else {
                check(table, player);
            }
        } else {
            if (bet >= table.getMinimumRaise()) {
                raise(table, player, bet);
            } else if (bet >= table.getMinimumBet()) {
                if (bet == player.getBet()) {
                    check(table, player);
                } else {
                    call(table, player);
                }
            } else {
                fold(table, player);
            }
        }
    }

    private boolean playerCanPayIt(final Table table, final Player player, final int bet) {
        if (player.isGoingAllIn(bet)) {
            player.bet(bet);
            logger.log(gameId, table.getId(), ALL_IN_TEXT, player.getName(), player.getBet());
            return false;
        }
        return true;
    }

    private void raise(final Table table, final Player player, final int bet) {
        if (playerCanPayIt(table, player, bet)) {
            logger.log(gameId, table.getId(), "Player %s raises to %s.", player.getName(), bet);
            player.bet(bet);
        }
    }

    private void call(final Table table, final Player player) {
        if (playerCanPayIt(table, player, table.getMinimumBet())) {
            logger.log(gameId, table.getId(), "Player %s calls the bet of %s.", player.getName(),
                    table.getMinimumBet());
            player.bet(table.getMinimumBet());
        }
    }

    private void bet(final Table table, final Player player, final int bet) {
        if (playerCanPayIt(table, player, bet)) {
            logger.log(gameId, table.getId(), "Player %s bets %s.", player.getName(), bet);
            player.bet(bet);
        }
    }

    private void fold(final Table table, final Player player) {
        logger.log(gameId, table.getId(), "Player %s folds.", player.getName());
        player.fold();
    }

    private void check(final Table table, final Player player) {
        logger.log(gameId, table.getId(), "Player %s checks.", player.getName());
    }

    private boolean noPlayerHasBetYet(final Table table) {
        return table.getPlayers().stream().mapToInt(Player::getBet).sum() == 0;
    }

}

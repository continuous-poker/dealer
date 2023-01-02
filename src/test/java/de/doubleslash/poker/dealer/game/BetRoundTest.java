package de.doubleslash.poker.dealer.game;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import de.doubleslash.poker.dealer.ActionProvider;
import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Status;
import de.doubleslash.poker.dealer.data.Table;
import org.junit.jupiter.api.Test;

class BetRoundTest {

    private static final long GAME_ID = 1;
    private static final int SMALL_BLIND = 5;
    private static final int START_STACK = 100;

    private final GameLogger logger = new GameLogger();

    @Test
    void preFlop() {
        final Player player1 = createPlayer("player1", table -> 0);
        final Player player2 = createPlayer("player2", Table::getMinimumBet);
        final List<Player> players = List.of(player1, player2);
        final Table table = new Table(GAME_ID, players, SMALL_BLIND, msg -> logger.log(GAME_ID, 1, 0, msg));
        final BetRound betRound = new BetRound(GAME_ID, table, players, true, logger);

        final Optional<Player> winner = betRound.run();

        assertThat(player1.getCurrentBet()).isEqualTo(SMALL_BLIND);
        assertThat(player2.getCurrentBet()).isEqualTo(SMALL_BLIND * 2);
        assertThat(winner).contains(player2);
    }

    @Test
    void afterFlop_player1Folds() {
        final Player player1 = createPlayer("player1", table -> 0);
        final Player player2 = createPlayer("player2", Table::getMinimumBet);
        final List<Player> players = List.of(player1, player2);
        final Table table = new Table(GAME_ID, players, SMALL_BLIND, msg -> logger.log(GAME_ID, 0, 0, msg));
        final BetRound betRound = new BetRound(GAME_ID, table, players, false, logger);

        final Optional<Player> winner = betRound.run();

        assertThat(player1.getCurrentBet()).isZero();
        assertThat(player2.getCurrentBet()).isEqualTo(SMALL_BLIND * 2);
        assertThat(winner).contains(player2);
    }

    @Test
    void afterFlop_betAndCall() {
        final Player player1 = createPlayer("player1", Table::getMinimumBet);
        final Player player2 = createPlayer("player2", Table::getMinimumBet);
        final List<Player> players = List.of(player1, player2);
        final Table table = new Table(GAME_ID, players, SMALL_BLIND, msg -> logger.log(GAME_ID, 0, 0, msg));
        final BetRound betRound = new BetRound(GAME_ID, table, players, false, logger);

        final Optional<Player> winner = betRound.run();

        assertThat(player1.getCurrentBet()).isEqualTo(SMALL_BLIND * 2);
        assertThat(player2.getCurrentBet()).isEqualTo(SMALL_BLIND * 2);
        assertThat(winner).isEmpty();
    }

    @Test
    void afterFlop_betRaiseAndCall() {
        final Player player1 = createPlayer("player1", Table::getMinimumRaise);
        final Player player2 = createPlayer("player2", Table::getMinimumBet);
        final List<Player> players = List.of(player1, player2);
        final Table table = new Table(GAME_ID, players, SMALL_BLIND, msg -> logger.log(GAME_ID, 0, 0, msg));
        final BetRound betRound = new BetRound(GAME_ID, table, players, false, logger);

        final Optional<Player> winner = betRound.run();

        assertThat(player1.getCurrentBet()).isEqualTo(SMALL_BLIND * 4);
        assertThat(player2.getCurrentBet()).isEqualTo(SMALL_BLIND * 4);
        assertThat(winner).isEmpty();
    }

    private Player createPlayer(final String name, final ActionProvider actionPlayer1) {
        return new Player(name, Status.ACTIVE, START_STACK, 0, actionPlayer1);
    }
}
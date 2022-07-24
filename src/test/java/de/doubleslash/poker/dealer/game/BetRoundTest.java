package de.doubleslash.poker.dealer.game;

import java.util.List;

import de.doubleslash.poker.dealer.ActionProvider;
import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Status;
import de.doubleslash.poker.dealer.data.Table;
import org.junit.jupiter.api.Test;

class BetRoundTest {

    private static final long GAME_ID = 1;
    private static final int SMALL_BLIND = 5;

    private final GameLogger logger = new GameLogger();

    @Test
    void run() {
        final Player player1 = createPlayer("player1", table -> 1);
        final Player player2 = createPlayer("player2", table -> 1);
        final List<Player> players = List.of(player1, player2);
        final Table table = new Table(GAME_ID, players, SMALL_BLIND);

        final BetRound betRound = new BetRound(GAME_ID, table, players, true, logger);

        betRound.run();
    }

    private Player createPlayer(final String name, final ActionProvider actionPlayer1) {
        return new Player(name, Status.ACTIVE, 100, 0, actionPlayer1);
    }
}
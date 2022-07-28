package de.doubleslash.poker.dealer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import de.doubleslash.poker.dealer.game.Game;
import org.junit.jupiter.api.Test;

class GameManagerTest {

    private final GameManager state = new GameManager(new GameLogger());

    @Test
    void testGameRun() {
        final Collection<Team> players = new ArrayList<>();
        final Team winnerTeam = new Team("team2", table -> Integer.MAX_VALUE);
        players.add(new Team("team1", table -> 0));
        players.add(winnerTeam);

        final Game testgame = state.runSingleGame("testgame", players);

        assertThat(testgame.getTeams()).hasSize(2);
        assertThat(winnerTeam.getScore()).isOne();
    }

}

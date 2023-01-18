package org.continuouspoker.dealer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.continuouspoker.dealer.game.Game;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class GameManagerTest {

    private final GameManager state = new GameManager();

    @Test
    void testGameRun() {
        final Collection<Team> players = new ArrayList<>();
        players.add(new Team("team1", table -> 0));
        players.add(new Team("team2", table -> Integer.MAX_VALUE));

        final Game testgame = state.runSingleGame("testgame", players);

        Assertions.assertThat(testgame.getTeams()).hasSize(2);
        assertThat(testgame.getTeams().stream().mapToLong(Team::getScore).sum()).isOne();
    }

}

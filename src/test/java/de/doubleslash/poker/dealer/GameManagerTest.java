package de.doubleslash.poker.dealer;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

public class GameManagerTest {

    private final GameManager state = new GameManager(new GameLogger());

    @Test
    public void testGameRun() {
        final Collection<Team> players = new ArrayList<>();
        players.add(new Team("team1", table -> 0));
        players.add(new Team("team2", table -> Integer.MAX_VALUE));

        state.runSingleGame("testgame", players);

    }

}

package org.continuouspoker.dealer.calculation;

import java.util.Comparator;

import org.continuouspoker.dealer.data.Player;

public class NegativeBetComparator implements Comparator<Player> {
    @Override
    public int compare(final Player o1, final Player o2) {
        return o2.getCurrentBet() - o1.getCurrentBet();
    }
}

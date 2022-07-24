package de.doubleslash.poker.dealer;

import de.doubleslash.poker.dealer.data.Table;

public class DummyPlayer implements ActionProvider {

    @Override
    public int requestBet(final Table table) {
        return 0;
    }

}

package org.continuouspoker.dealer;

import org.continuouspoker.dealer.data.Table;

public class DummyPlayer implements ActionProvider {

    @Override
    public int requestBet(final Table table, final StepLogger logger) {
        return 0;
    }

}

package org.continuouspoker.dealer;

import org.continuouspoker.dealer.data.Table;

public interface ActionProvider {

    default String getUrl() {
        return "";
    }

    int requestBet(Table table, StepLogger logger);

}

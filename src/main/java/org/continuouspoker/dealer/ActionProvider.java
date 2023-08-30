package org.continuouspoker.dealer;

import java.time.Duration;

import org.continuouspoker.dealer.data.Table;

public interface ActionProvider {

    Duration READ_TIMEOUT = Duration.ofSeconds(30);
    int MAX_STRIKES = 3;

    int requestBet(Table table);

}

package org.continuouspoker.dealer;

import java.time.Duration;

import org.continuouspoker.dealer.data.Table;

public interface ActionProvider {

    public static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    public static final int MAX_STRIKES = 3;

    int requestBet(Table table);

}

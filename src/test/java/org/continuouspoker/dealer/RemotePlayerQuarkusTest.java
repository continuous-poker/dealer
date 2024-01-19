/*
 * Copyright © 2024 DoubleSlash Net-Business GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.continuouspoker.dealer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Pot;
import org.continuouspoker.dealer.data.Rank;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Suit;
import org.continuouspoker.dealer.data.Table;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
class RemotePlayerQuarkusTest {

    @ConfigProperty(name = "quarkus.rest-client.\"org.continuouspoker.dealer.RemotePlayerClient\".url")
    String wiremockUrl;

    @Test
    void requestBodyShouldMatchExpectedFormat() {
        final RemotePlayer remotePlayer = new RemotePlayer(wiremockUrl);

        final int bet = remotePlayer.requestBet(createTable(), msg -> {});

        assertThat(bet).isEqualTo(5);

    }

    private static Table createTable() {
        final Player bot1 = new Player("Bot1", Status.ACTIVE, 990, 10, (table, logger) -> 1);
        final Player bot2 = new Player("Bot2", Status.ACTIVE, 980, 20, (table, logger) -> 1);
        final Player bot3 = new Player("Bot3", Status.ACTIVE, 1000, 0, (table, logger) -> 1);
        bot3.takeCard(new Card(Rank.TWO, Suit.HEARTS));
        bot3.takeCard(new Card(Rank.JACK, Suit.CLUBS));
        final Table table = new Table(1, List.of(bot1, bot2, bot3), 10);
        table.setPot(new Pot(msg -> {}));

        table.takeCard(new Card(Rank.TEN, Suit.HEARTS));
        table.takeCard(new Card(Rank.FIVE, Suit.CLUBS));
        table.takeCard(new Card(Rank.ACE, Suit.DIAMONDS));

        table.setActivePlayer(bot3);
        return table;
    }

}

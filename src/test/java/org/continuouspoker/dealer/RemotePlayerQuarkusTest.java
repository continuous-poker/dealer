package org.continuouspoker.dealer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Player;
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

        final int bet = remotePlayer.requestBet(createTable());

        assertThat(bet).isEqualTo(5);

    }

    private static Table createTable() {
        final Player bot1 = new Player("Bot1", Status.ACTIVE, 990, 10, a -> 1);
        final Player bot2 = new Player("Bot2", Status.ACTIVE, 980, 20, a -> 1);
        final Player bot3 = new Player("Bot3", Status.ACTIVE, 1000, 0, a -> 1);
        bot3.takeCard(new Card(Rank.TWO, Suit.HEARTS));
        bot3.takeCard(new Card(Rank.JACK, Suit.CLUBS));
        final Table table = new Table(1, List.of(bot1, bot2, bot3), 10, a -> {
        });

        table.takeCard(new Card(Rank.TEN, Suit.HEARTS));
        table.takeCard(new Card(Rank.FIVE, Suit.CLUBS));
        table.takeCard(new Card(Rank.ACE, Suit.DIAMONDS));

        table.setActivePlayer(bot3);
        return table;
    }

}

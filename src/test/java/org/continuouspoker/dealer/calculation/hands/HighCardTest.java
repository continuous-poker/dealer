package org.continuouspoker.dealer.calculation.hands;

import static org.continuouspoker.dealer.data.Rank.FIVE;
import static org.continuouspoker.dealer.data.Rank.FOUR;
import static org.continuouspoker.dealer.data.Rank.JACK;
import static org.continuouspoker.dealer.data.Rank.KING;
import static org.continuouspoker.dealer.data.Rank.NINE;
import static org.continuouspoker.dealer.data.Rank.TEN;
import static org.continuouspoker.dealer.data.Rank.TWO;
import static org.continuouspoker.dealer.data.Suit.CLUBS;
import static org.continuouspoker.dealer.data.Suit.HEARTS;
import static org.continuouspoker.dealer.data.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.continuouspoker.dealer.data.Card;
import org.junit.jupiter.api.Test;

public class HighCardTest {

    private final HighCard strategy = new HighCard();

    @Test
    public void testCalculate() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(TWO, CLUBS), new Card(FIVE, SPADES),
                new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(FOUR, SPADES), new Card(NINE, HEARTS));
        Collections.shuffle(cards);

        final int[] score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 0,
                                      13,
                                      11,
                                      10,
                                      9,
                                      5
        }, score);
    }

}

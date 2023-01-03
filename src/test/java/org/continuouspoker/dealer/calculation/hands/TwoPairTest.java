package org.continuouspoker.dealer.calculation.hands;

import static org.continuouspoker.dealer.data.Rank.ACE;
import static org.continuouspoker.dealer.data.Rank.FIVE;
import static org.continuouspoker.dealer.data.Rank.FOUR;
import static org.continuouspoker.dealer.data.Rank.NINE;
import static org.continuouspoker.dealer.data.Rank.QUEEN;
import static org.continuouspoker.dealer.data.Suit.CLUBS;
import static org.continuouspoker.dealer.data.Suit.HEARTS;
import static org.continuouspoker.dealer.data.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.continuouspoker.dealer.data.Card;
import org.junit.jupiter.api.Test;

public class TwoPairTest {

    private final TwoPair strategy = new TwoPair();

    @Test
    public void testCalculate() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(ACE, SPADES), new Card(QUEEN, SPADES), new Card(FIVE, HEARTS),
                new Card(ACE, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(NINE, SPADES));
        Collections.shuffle(cards);

        final int[] score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 2,
                                      28,
                                      10,
                                      12
        }, score);
    }
}

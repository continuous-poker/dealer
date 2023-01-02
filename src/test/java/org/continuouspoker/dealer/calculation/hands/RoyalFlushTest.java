package org.continuouspoker.dealer.calculation.hands;

import static org.continuouspoker.dealer.data.Rank.ACE;
import static org.continuouspoker.dealer.data.Rank.JACK;
import static org.continuouspoker.dealer.data.Rank.KING;
import static org.continuouspoker.dealer.data.Rank.NINE;
import static org.continuouspoker.dealer.data.Rank.QUEEN;
import static org.continuouspoker.dealer.data.Rank.TEN;
import static org.continuouspoker.dealer.data.Rank.THREE;
import static org.continuouspoker.dealer.data.Suit.CLUBS;
import static org.continuouspoker.dealer.data.Suit.HEARTS;
import static org.continuouspoker.dealer.data.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.continuouspoker.dealer.data.Card;
import org.junit.jupiter.api.Test;

class RoyalFlushTest {

    private final RoyalFlush strategy = new RoyalFlush();

    @Test
    void testCalculate_withLongStraight() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, CLUBS), new Card(QUEEN, SPADES),
                new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));
        Collections.shuffle(cards);

        final int[] score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 9
        }, score);
    }

    @Test
    void testMatches_doesMatch() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, SPADES),
                new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));

        assertTrue(strategy.matches(cards));
    }


    @Test
    void testMatches_doesNotMatch() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, HEARTS),
                new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));

        assertFalse(strategy.matches(cards));
    }

    @Test
    void testMatches_missingTen() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, SPADES),
                new Card(JACK, SPADES), new Card(TEN, HEARTS), new Card(NINE, SPADES), new Card(THREE, HEARTS));

        assertFalse(strategy.matches(cards));
    }

}

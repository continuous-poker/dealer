package org.continuouspoker.dealer.calculation.hands;

import static org.continuouspoker.dealer.data.Rank.ACE;
import static org.continuouspoker.dealer.data.Rank.FIVE;
import static org.continuouspoker.dealer.data.Rank.FOUR;
import static org.continuouspoker.dealer.data.Rank.KING;
import static org.continuouspoker.dealer.data.Rank.SEVEN;
import static org.continuouspoker.dealer.data.Rank.SIX;
import static org.continuouspoker.dealer.data.Rank.THREE;
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

class StraightTest {

    private final Straight strategy = new Straight();

    @Test
    void testCalculate_withLongStraight() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(SIX, SPADES), new Card(ACE, CLUBS), new Card(FIVE, HEARTS),
                new Card(TWO, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(THREE, SPADES));
        Collections.shuffle(cards);

        final Score score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 4,
                                      6
        }, score.scoreRank());
    }

    @Test
    void testCalculate() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(SIX, SPADES), new Card(ACE, CLUBS), new Card(SEVEN, HEARTS),
                new Card(KING, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(THREE, SPADES));
        Collections.shuffle(cards);

        final Score score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 4,
                                      7
        }, score.scoreRank());
    }

    @Test
    void testCalculate_withLowEndAce() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(ACE, SPADES), new Card(FIVE, CLUBS), new Card(FIVE, HEARTS),
                new Card(TWO, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(THREE, SPADES));
        Collections.shuffle(cards);

        final Score score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 4,
                                      5
        }, score.scoreRank());
    }
}

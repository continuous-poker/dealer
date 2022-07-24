package de.doubleslash.poker.dealer.calculation.hands;

import static de.doubleslash.poker.dealer.data.Rank.ACE;
import static de.doubleslash.poker.dealer.data.Rank.FIVE;
import static de.doubleslash.poker.dealer.data.Rank.FOUR;
import static de.doubleslash.poker.dealer.data.Rank.KING;
import static de.doubleslash.poker.dealer.data.Rank.SEVEN;
import static de.doubleslash.poker.dealer.data.Rank.SIX;
import static de.doubleslash.poker.dealer.data.Rank.THREE;
import static de.doubleslash.poker.dealer.data.Rank.TWO;
import static de.doubleslash.poker.dealer.data.Suit.CLUBS;
import static de.doubleslash.poker.dealer.data.Suit.HEARTS;
import static de.doubleslash.poker.dealer.data.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.doubleslash.poker.dealer.data.Card;
import org.junit.jupiter.api.Test;

public class StraightTest {

    private final Straight strategy = new Straight();

    @Test
    public void testCalculate_withLongStraight() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(SIX, SPADES), new Card(ACE, CLUBS), new Card(FIVE, HEARTS),
                new Card(TWO, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(THREE, SPADES));
        Collections.shuffle(cards);

        final int[] score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 4,
                                      6
        }, score);
    }

    @Test
    public void testCalculate() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(SIX, SPADES), new Card(ACE, CLUBS), new Card(SEVEN, HEARTS),
                new Card(KING, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(THREE, SPADES));
        Collections.shuffle(cards);

        final int[] score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 4,
                                      7
        }, score);
    }

    @Test
    public void testCalculate_withLowEndAce() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(ACE, SPADES), new Card(FIVE, CLUBS), new Card(FIVE, HEARTS),
                new Card(TWO, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(THREE, SPADES));
        Collections.shuffle(cards);

        final int[] score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 4,
                                      5
        }, score);
    }
}

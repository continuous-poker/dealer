package de.doubleslash.poker.dealer.calculation.hands;

import static de.doubleslash.poker.dealer.data.Rank.ACE;
import static de.doubleslash.poker.dealer.data.Rank.FIVE;
import static de.doubleslash.poker.dealer.data.Rank.FOUR;
import static de.doubleslash.poker.dealer.data.Rank.NINE;
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

public class ThreeOfAKindTest {

    private final ThreeOfAKind strategy = new ThreeOfAKind();

    @Test
    public void testCalculate() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(ACE, SPADES), new Card(FIVE, CLUBS), new Card(FIVE, HEARTS),
                new Card(TWO, CLUBS), new Card(FOUR, SPADES), new Card(FIVE, SPADES), new Card(NINE, SPADES));
        Collections.shuffle(cards);

        final int[] score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 3, 15, 14, 9 }, score);
    }
}

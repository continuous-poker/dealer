package de.doubleslash.poker.dealer.calculation.hands;

import static de.doubleslash.poker.dealer.data.Rank.ACE;
import static de.doubleslash.poker.dealer.data.Rank.JACK;
import static de.doubleslash.poker.dealer.data.Rank.KING;
import static de.doubleslash.poker.dealer.data.Rank.NINE;
import static de.doubleslash.poker.dealer.data.Rank.QUEEN;
import static de.doubleslash.poker.dealer.data.Rank.TEN;
import static de.doubleslash.poker.dealer.data.Rank.THREE;
import static de.doubleslash.poker.dealer.data.Suit.CLUBS;
import static de.doubleslash.poker.dealer.data.Suit.HEARTS;
import static de.doubleslash.poker.dealer.data.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;



import de.doubleslash.poker.dealer.calculation.hands.RoyalFlush;
import de.doubleslash.poker.dealer.data.Card;
import org.junit.jupiter.api.Test;

public class RoyalFlushTest {

   private final RoyalFlush strategy = new RoyalFlush();

   @Test
   public void testCalculate_withLongStraight() throws Exception {
      final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, CLUBS), new Card(QUEEN, SPADES),
            new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));
      Collections.shuffle(cards);

      final int[] score = strategy.calculateScore(cards);

      assertArrayEquals(new int[] {
            9
      }, score);
   }

   @Test
   public void testMatches_doesMatch() throws Exception {
      final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, SPADES),
            new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));

      assertTrue(strategy.matches(cards));
   }

   @Test
   public void testMatches_doesNotMatch() throws Exception {
      final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, HEARTS),
            new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));

      assertFalse(strategy.matches(cards));
   }
}

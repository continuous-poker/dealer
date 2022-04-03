package de.doubleslash.poker.dealer.calculation.hands;

import java.util.List;

import de.doubleslash.poker.dealer.data.Card;

public interface PokerHand {

   int[] calculateScore(List<Card> cards);

   boolean matches(List<Card> cards);
}

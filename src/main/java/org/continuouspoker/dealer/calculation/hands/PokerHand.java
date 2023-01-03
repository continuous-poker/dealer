package org.continuouspoker.dealer.calculation.hands;

import java.util.List;

import org.continuouspoker.dealer.data.Card;

public interface PokerHand {

    int[] calculateScore(List<Card> cards);

    boolean matches(List<Card> cards);
}

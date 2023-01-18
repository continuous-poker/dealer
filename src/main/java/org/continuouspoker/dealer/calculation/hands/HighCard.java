package org.continuouspoker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.continuouspoker.dealer.data.Card;

public class HighCard implements PokerHand {

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);
        Collections.sort(cards);
        // [0,2-14,2-14,2-14,2-14,2-14]

        // just drop the lowest two cards
        cards.remove(cards.size() - 1);
        cards.remove(cards.size() - 1);

        return new Score("High Card",
                IntStream.concat(IntStream.of(0), cards.stream().mapToInt(Card::getValue)).toArray());
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        return true;
    }

}

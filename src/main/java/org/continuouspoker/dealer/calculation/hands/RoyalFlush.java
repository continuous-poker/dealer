package org.continuouspoker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Rank;
import org.continuouspoker.dealer.data.Suit;

public class RoyalFlush implements PokerHand {

    private static final int SCORE = 9;
    private static final int NUMBER_OF_CARDS = 5;

    @Override
    public int[] calculateScore(final List<Card> cardsToScore) {
        // [9]
        return new int[] { SCORE
        };
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        final Optional<List<Card>> flush = collect.values().stream().filter(list -> list.size() >= NUMBER_OF_CARDS).findFirst();
        if (flush.isPresent()) {
            final List<Card> sequenceCards = getSequenceCards(flush.get());
            if (sequenceCards.size() >= NUMBER_OF_CARDS) {
                Collections.sort(sequenceCards);
                return sequenceCards.get(0).getRank() == Rank.ACE;
            }
        }
        return false;
    }

    private List<Card> getSequenceCards(final List<Card> cards) {
        Collections.sort(cards);
        final List<Card> sequenceCards = new ArrayList<>();
        Card previousCard = null;
        for (final Card card : cards) {
            if (previousCard != null) {
                if (previousCard.getValue() - 1 == card.getValue()) {
                    sequenceCards.add(card);
                } else if (previousCard.getValue() == card.getValue()) {
                    continue;
                } else {
                    if (sequenceCards.size() < NUMBER_OF_CARDS) {
                        sequenceCards.clear();
                    }
                }
            } else {
                sequenceCards.add(card);
            }
            previousCard = card;
        }
        return sequenceCards;
    }

    private Map<Suit, List<Card>> getCardsGroupedBySuit(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getSuit));
    }

}

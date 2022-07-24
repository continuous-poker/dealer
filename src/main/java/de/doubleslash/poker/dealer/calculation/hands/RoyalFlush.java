package de.doubleslash.poker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Rank;
import de.doubleslash.poker.dealer.data.Suit;

public class RoyalFlush implements PokerHand {

    @Override
    public int[] calculateScore(final List<Card> cardsToScore) {
        // [9]
        return new int[] { 9
        };
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        final Optional<List<Card>> flush = collect.values().stream().filter(list -> list.size() >= 5).findFirst();
        if (flush.isPresent()) {
            final List<Card> sequenceCards = getSequenceCards(flush.get());
            if (sequenceCards.size() >= 5) {
                Collections.sort(sequenceCards);
                return sequenceCards.get(0).getRank() == Rank.ACE;
            }
        }
        return false;
    }

    private List<Card> getSequenceCards(final List<Card> cards) {
        Collections.sort(cards);
        final List<Card> sequenceCards = new ArrayList<>();
        int sequenceCounter = 0;
        Card lastCard = null;
        for (final Card card : cards) {
            if (lastCard != null) {
                if (lastCard.getValue() - 1 == card.getValue()) {
                    sequenceCards.add(card);
                    sequenceCounter++;
                } else if (lastCard.getValue() == card.getValue()) {
                    continue;
                } else {
                    if (sequenceCounter < 4) {
                        sequenceCounter = 0;
                        sequenceCards.clear();
                    }
                }
            } else {
                sequenceCards.add(card);
            }
            lastCard = card;
        }
        return sequenceCards;
    }

    private Map<Suit, List<Card>> getCardsGroupedBySuit(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getSuit));
    }

}

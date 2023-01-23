package org.continuouspoker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Rank;
import org.continuouspoker.dealer.data.Suit;

public class StraightFlush implements PokerHand {

    private static final int SCORE = 8;
    private static final int NUMBER_OF_CARDS = 5;

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        // [8,2-14]
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        final Optional<List<Card>> flush = collect.values()
                                                  .stream()
                                                  .filter(list -> list.size() >= NUMBER_OF_CARDS)
                                                  .findFirst();
        final List<Card> sequenceCards = getSequenceCards(flush.orElseThrow(IllegalStateException::new));
        Collections.sort(sequenceCards);
        int highestValue = sequenceCards.get(0).getValue();
        if (highestValue == Rank.ACE.getValue() && doesNotContainKing(sequenceCards)) {
            // Straight is A-5, so 5 is the highest card, not A
            highestValue = sequenceCards.get(1).getValue();
        }
        return new Score("Straight flush", IntStream.of(SCORE, highestValue).toArray());
    }

    private boolean doesNotContainKing(final List<Card> sequenceCards) {
        return sequenceCards.stream().map(Card::getRank).noneMatch(Rank.KING::equals);
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        final Optional<List<Card>> flush = collect.values()
                                                  .stream()
                                                  .filter(list -> list.size() >= NUMBER_OF_CARDS)
                                                  .findFirst();
        return flush.filter(this::hasSequence).isPresent();
    }

    private boolean hasSequence(final List<Card> cards) {
        final List<Card> sequenceCards = getSequenceCards(cards);
        return sequenceCards.size() >= NUMBER_OF_CARDS;
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

                    sequenceCounter = checkForLowEndAce(cards, sequenceCards, sequenceCounter, card);
                } else if (lastCard.getValue() == card.getValue()) {
                    continue;
                } else {
                    if (sequenceCounter < NUMBER_OF_CARDS) {
                        sequenceCounter = 1;
                        sequenceCards.clear();
                        sequenceCards.add(card);
                    }
                }
            } else {
                sequenceCards.add(card);
                sequenceCounter++;
            }
            lastCard = card;
        }
        return sequenceCards;
    }

    private boolean containsAce(final List<Card> cards) {
        return cards.stream().map(Card::getRank).anyMatch(Rank.ACE::equals);
    }

    private int checkForLowEndAce(final List<Card> cards, final List<Card> sequenceCards, final int sequenceCounter,
            final Card card) {
        if (card.getRank() == Rank.TWO && containsAce(cards)) {
            // ace can be used at the low end of a straight too
            sequenceCards.add(getAce(cards));
            return sequenceCounter + 1;
        }
        return sequenceCounter;
    }

    private Card getAce(final List<Card> cards) {
        return cards.stream()
                    .filter(card -> card.getRank().equals(Rank.ACE))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
    }

    private Map<Suit, List<Card>> getCardsGroupedBySuit(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getSuit));
    }

}

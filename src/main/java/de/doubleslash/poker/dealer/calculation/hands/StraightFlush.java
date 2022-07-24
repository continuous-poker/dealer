package de.doubleslash.poker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Rank;
import de.doubleslash.poker.dealer.data.Suit;

public class StraightFlush implements PokerHand {

    @Override
    public int[] calculateScore(final List<Card> cardsToScore) {
        // [8,2-14]
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        final Optional<List<Card>> flush = collect.values().stream().filter(list -> list.size() >= 5).findFirst();
        final List<Card> sequenceCards = getSequenceCards(flush.orElseThrow(IllegalStateException::new));
        Collections.sort(sequenceCards);
        int highestValue = sequenceCards.get(0).getValue();
        if (highestValue == Rank.ACE.getValue() && doesNotContainKing(sequenceCards)) {
            // Straight is A-5, so 5 is the highest card, not A
            highestValue = sequenceCards.get(1).getValue();
        }
        return IntStream.of(8, highestValue).toArray();
    }

    private boolean doesNotContainKing(final List<Card> sequenceCards) {
        return sequenceCards.stream().map(Card::getRank).noneMatch(Rank.KING::equals);
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        final Optional<List<Card>> flush = collect.values().stream().filter(list -> list.size() >= 5).findFirst();
        if (flush.isPresent()) {
            return hasSequence(flush.get());
        }
        return false;
    }

    private boolean hasSequence(final List<Card> cards) {
        final List<Card> sequenceCards = getSequenceCards(cards);
        return sequenceCards.size() >= 5;
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
                    if (sequenceCounter < 4) {
                        sequenceCounter = 0;
                        sequenceCards.clear();
                        sequenceCards.add(card);
                    }
                }
            } else {
                sequenceCards.add(card);
            }
            lastCard = card;
        }
        return sequenceCards;
    }

    private boolean containsAce(final List<Card> cards) {
        return cards.stream().map(Card::getRank).anyMatch(Rank.ACE::equals);
    }

    private int checkForLowEndAce(final List<Card> cards, final List<Card> sequenceCards, int sequenceCounter,
            final Card card) {
        if (card.getRank() == Rank.TWO && containsAce(cards)) {
            // ace can be used at the low end of a straight too
            sequenceCounter++;
            sequenceCards.add(getAce(cards));
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

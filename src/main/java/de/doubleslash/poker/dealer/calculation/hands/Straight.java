package de.doubleslash.poker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Rank;

public class Straight implements PokerHand {

    private static final int SCORE = 4;
    private static final int NUMBER_OF_CARDS = 5;

    @Override
    public int[] calculateScore(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);
        // [4,2-14]
        final List<Card> sequenceCards = getSequenceCards(cards);
        Collections.sort(sequenceCards);

        int highestValue = sequenceCards.get(0).getValue();
        if (highestValue == Rank.ACE.getValue() && doesNotContainKing(sequenceCards)) {
            // Straight is A-5, so 5 is the highest card, not A
            highestValue = sequenceCards.get(1).getValue();
        }
        return IntStream.of(SCORE, highestValue).toArray();
    }

    private boolean doesNotContainKing(final List<Card> sequenceCards) {
        return sequenceCards.stream().map(Card::getRank).noneMatch(Rank.KING::equals);
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        return hasSequence(cardsToScore);
    }

    private boolean hasSequence(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);

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

    private boolean containsAce(final List<Card> cards) {
        return cards.stream().map(Card::getRank).anyMatch(Rank.ACE::equals);
    }

}

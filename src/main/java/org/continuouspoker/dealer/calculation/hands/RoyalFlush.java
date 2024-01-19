/*
 * Copyright © 2024 DoubleSlash Net-Business GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public Score calculateScore(final List<Card> cardsToScore) {
        // [9]
        return new Score("Royal Flush", new int[] { SCORE
        });
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        final Optional<List<Card>> flush = collect.values()
                                                  .stream()
                                                  .filter(list -> list.size() >= NUMBER_OF_CARDS)
                                                  .findFirst();
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

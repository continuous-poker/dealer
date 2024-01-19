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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Suit;

public class Flush implements PokerHand {

    private static final int NUMBER_OF_CARDS = 5;
    private static final int SCORE = 5;

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        // [5,2-14,2-14,2-14,2-14,2-14]
        final Map<Suit, List<Card>> cardsGroupedBySuit = getCardsGroupedBySuit(cardsToScore);
        final List<Card> flush = getFlushCards(cardsGroupedBySuit);
        Collections.sort(flush);

        while (flush.size() > NUMBER_OF_CARDS) {
            flush.remove(flush.size() - 1);
        }

        return new Score("Flush",
                IntStream.concat(IntStream.of(SCORE), flush.stream().mapToInt(Card::getValue)).toArray());
    }

    private List<Card> getFlushCards(final Map<Suit, List<Card>> cardsGroupedBySuit) {
        final Optional<List<Card>> findFirst = cardsGroupedBySuit.values()
                                                                 .stream()
                                                                 .filter(cards -> cards.size() >= NUMBER_OF_CARDS)
                                                                 .findFirst();
        return findFirst.orElseThrow(IllegalStateException::new);
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
        return collect.values().stream().anyMatch(list -> list.size() >= NUMBER_OF_CARDS);
    }

    private Map<Suit, List<Card>> getCardsGroupedBySuit(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getSuit));
    }

}

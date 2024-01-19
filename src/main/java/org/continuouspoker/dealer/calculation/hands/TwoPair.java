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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Rank;

public class TwoPair implements PokerHand {

    private static final int SCORE = 2;
    private static final int NUMBER_OF_CARDS = 2;
    private static final int NUMBER_OF_PAIRS = 2;

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        // [2,4-28,4-28,2-14]

        final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cardsToScore);

        final int pairScore1 = getAndRemoveHighestPair(cardsGroupedByRank);

        final int pairScore2 = getAndRemoveHighestPair(cardsGroupedByRank);

        final Card kicker = getHighestCard(cardsGroupedByRank);

        return new Score("Two Pair", IntStream.of(SCORE, pairScore1, pairScore2, kicker.getValue()).toArray());

    }

    private Card getHighestCard(final Map<Rank, List<Card>> cardsGroupedByRank) {
        return cardsGroupedByRank.values()
                                 .stream()
                                 .findFirst()
                                 .map(list -> list.get(0))
                                 .orElseThrow(IllegalStateException::new);
    }

    private int getAndRemoveHighestPair(final Map<Rank, List<Card>> cardsGroupedByRank) {
        final Rank rank = cardsGroupedByRank.entrySet()
                                            .stream()
                                            .filter(e -> e.getValue().size() >= NUMBER_OF_CARDS)
                                            .map(Entry::getKey)
                                            .findFirst()
                                            .orElseThrow(IllegalStateException::new);
        final int pairScore = rank.getValue() * NUMBER_OF_CARDS;
        cardsGroupedByRank.remove(rank);
        return pairScore;
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cardsToScore);

        return cardsGroupedByRank.values().stream().filter(list -> list.size() >= NUMBER_OF_CARDS).count()
                >= NUMBER_OF_PAIRS;
    }

    private Map<Rank, List<Card>> getCardsGroupedByRank(final List<Card> cardsToScore) {
        final Map<Rank, List<Card>> cardsGroupedByRank = cardsToScore.stream()
                                                                     .collect(Collectors.groupingBy(Card::getRank));
        return new TreeMap<>(cardsGroupedByRank);
    }

}

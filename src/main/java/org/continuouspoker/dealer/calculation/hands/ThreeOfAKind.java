/*
 * Copyright © 2020 - 2024 Jan Kreutzfeld
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
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Rank;

public class ThreeOfAKind implements PokerHand {

    private static final int SCORE = 3;
    private static final int NUMBER_OF_CARDS = 3;

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);
        // [3,6-42,2-14,2-14]

        final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cardsToScore);
        final List<Card> triplets = getTriplets(cardsGroupedByRank);

        final int tripletScore = triplets.stream().mapToInt(Card::getValue).sum();

        cards.removeAll(triplets);

        Collections.sort(cards);

        return new Score("Three Of A Kind",
                IntStream.of(SCORE, tripletScore, cards.get(0).getValue(), cards.get(1).getValue()).toArray());

    }

    private List<Card> getTriplets(final Map<Rank, List<Card>> cardsGroupedBySuit) {
        final Map<Rank, List<Card>> sortedMap = new TreeMap<>(cardsGroupedBySuit);
        final Optional<List<Card>> findFirst = sortedMap.values()
                                                        .stream()
                                                        .filter(cards -> cards.size() == NUMBER_OF_CARDS)
                                                        .findFirst();
        return findFirst.orElseThrow(IllegalStateException::new);
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Rank, List<Card>> collect = getCardsGroupedByRank(cardsToScore);
        return collect.values().stream().anyMatch(list -> list.size() == NUMBER_OF_CARDS);
    }

    private Map<Rank, List<Card>> getCardsGroupedByRank(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getRank));
    }

}

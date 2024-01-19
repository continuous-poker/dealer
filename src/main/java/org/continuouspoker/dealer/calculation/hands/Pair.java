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
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Rank;

public class Pair implements PokerHand {

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);
        // [1,4-28,2-14,2-14,2-14]

        final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cards);
        final List<Card> pairs = getPairs(cardsGroupedByRank);

        final int pairScore = pairs.stream().mapToInt(Card::getValue).limit(2).sum();

        cards.removeAll(pairs);

        Collections.sort(cards);

        return new Score("Pair",
                IntStream.of(1, pairScore, cards.get(0).getValue(), cards.get(1).getValue(), cards.get(2).getValue())
                         .toArray());

    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Rank, List<Card>> collect = getCardsGroupedByRank(cardsToScore);
        return collect.values().stream().anyMatch(list -> list.size() >= 2);
    }

    private List<Card> getPairs(final Map<Rank, List<Card>> cardsGroupedByRank) {
        final Map<Rank, List<Card>> sortedMap = new TreeMap<>(cardsGroupedByRank);
        final Optional<List<Card>> findFirst = sortedMap.values().stream().filter(list -> list.size() >= 2).findFirst();
        return findFirst.orElseThrow(IllegalStateException::new);
    }

    private Map<Rank, List<Card>> getCardsGroupedByRank(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getRank));
    }

}

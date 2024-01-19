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

public class FullHouse implements PokerHand {

    private static final int SCORE = 6;
    private static final int TRIPLET_SIZE = 3;
    private static final int PAIR_SIZE = 2;

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);
        // [6,6-42,4-28]

        final int tripletScore = getTripletScore(cards);
        final int pairScore = getPairScore(cards);

        return new Score("Full House", IntStream.of(SCORE, tripletScore, pairScore).toArray());

    }

    private int getTripletScore(final List<Card> cards) {
        final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cards);
        final List<Card> triplets = getTriplets(cardsGroupedByRank);
        final int tripletScore = triplets.stream().mapToInt(Card::getValue).sum();
        cards.removeAll(triplets);
        Collections.sort(cards);
        return tripletScore;
    }

    private int getPairScore(final List<Card> cards) {
        final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cards);
        final List<Card> pair = getPair(cardsGroupedByRank);
        return pair.stream().mapToInt(Card::getValue).limit(PAIR_SIZE).sum();
    }

    private List<Card> getTriplets(final Map<Rank, List<Card>> cardsGroupedBySuit) {
        final Map<Rank, List<Card>> sortedMap = new TreeMap<>(cardsGroupedBySuit);
        final Optional<List<Card>> findFirst = sortedMap.values()
                                                        .stream()
                                                        .filter(cards -> cards.size() == TRIPLET_SIZE)
                                                        .findFirst();
        return findFirst.orElseThrow(IllegalStateException::new);
    }

    private List<Card> getPair(final Map<Rank, List<Card>> cardsGroupedBySuit) {
        final Map<Rank, List<Card>> sortedMap = new TreeMap<>(cardsGroupedBySuit);
        final Optional<List<Card>> findFirst = sortedMap.values()
                                                        .stream()
                                                        .filter(cards -> cards.size() >= PAIR_SIZE)
                                                        .findFirst();
        return findFirst.orElseThrow(IllegalStateException::new);
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Rank, List<Card>> collect = getCardsGroupedByRank(cardsToScore);
        boolean tripletMatch = false;
        boolean pairMatch = false;
        for (final List<Card> cards : collect.values()) {
            if (cards.size() == TRIPLET_SIZE) {
                if (tripletMatch) {
                    pairMatch = true;
                } else {
                    tripletMatch = true;
                }
            } else if (cards.size() == PAIR_SIZE) {
                pairMatch = true;
            }
        }

        return tripletMatch && pairMatch;

    }

    private Map<Rank, List<Card>> getCardsGroupedByRank(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getRank));
    }

}

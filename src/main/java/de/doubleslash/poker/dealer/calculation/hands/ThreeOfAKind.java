package de.doubleslash.poker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Rank;

public class ThreeOfAKind implements PokerHand {

    @Override
    public int[] calculateScore(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);
        // [3,6-42,2-14,2-14]

        final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cardsToScore);
        final List<Card> triplets = getTriplets(cardsGroupedByRank);

        final int tripletScore = triplets.stream().mapToInt(Card::getValue).sum();

        cards.removeAll(triplets);

        Collections.sort(cards);

        return IntStream.of(3, tripletScore, cards.get(0).getValue(), cards.get(1).getValue()).toArray();

    }

    private List<Card> getTriplets(final Map<Rank, List<Card>> cardsGroupedBySuit) {
        final Map<Rank, List<Card>> sortedMap = new TreeMap<>(cardsGroupedBySuit);
        final Optional<List<Card>> findFirst = sortedMap.entrySet()
                                                        .stream()
                                                        .filter(entry -> entry.getValue().size() == 3)
                                                        .map(Entry::getValue)
                                                        .findFirst();
        return findFirst.orElseThrow(IllegalStateException::new);
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        final Map<Rank, List<Card>> collect = getCardsGroupedByRank(cardsToScore);
        return collect.values().stream().anyMatch(list -> list.size() == 3);
    }

    private Map<Rank, List<Card>> getCardsGroupedByRank(final List<Card> cardsToScore) {
        return cardsToScore.stream().collect(Collectors.groupingBy(Card::getRank));
    }

}

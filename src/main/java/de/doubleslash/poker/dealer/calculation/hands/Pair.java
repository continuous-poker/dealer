package de.doubleslash.poker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Rank;

public class Pair implements PokerHand {

   @Override
   public int[] calculateScore(final List<Card> cardsToScore) {
      final List<Card> cards = new ArrayList<>(cardsToScore);
      // [1,4-28,2-14,2-14,2-14]

      final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cards);
      final List<Card> pairs = getPairs(cardsGroupedByRank);

      final int pairScore = pairs.stream().mapToInt(Card::getValue).limit(2).sum();

      cards.removeAll(pairs);

      Collections.sort(cards);

      return IntStream.of(1, pairScore, cards.get(0).getValue(), cards.get(1).getValue(), cards.get(2).getValue())
            .toArray();

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

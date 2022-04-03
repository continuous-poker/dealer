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

public class FourOfAKind implements PokerHand {

   @Override
   public int[] calculateScore(final List<Card> cardsToScore) {
      final List<Card> cards = new ArrayList<>(cardsToScore);
      // [7,8-56,2-14]

      final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cardsToScore);
      final List<Card> quads = getQuads(cardsGroupedByRank);

      final int quadScore = quads.stream().mapToInt(Card::getValue).sum();

      cards.removeAll(quads);

      Collections.sort(cards);

      return IntStream.of(7, quadScore, cards.get(0).getValue()).toArray();

   }

   private List<Card> getQuads(final Map<Rank, List<Card>> cardsGroupedBySuit) {
      final Map<Rank, List<Card>> sortedMap = new TreeMap<>(cardsGroupedBySuit);
      final Optional<List<Card>> findFirst = sortedMap.entrySet().stream().filter(entry -> entry.getValue().size() == 4)
            .map(Entry::getValue).findFirst();
      return findFirst.orElseThrow(IllegalStateException::new);
   }

   @Override
   public boolean matches(final List<Card> cardsToScore) {
      final Map<Rank, List<Card>> collect = getCardsGroupedByRank(cardsToScore);
      return collect.values().stream().anyMatch(list -> list.size() == 4);
   }

   private Map<Rank, List<Card>> getCardsGroupedByRank(final List<Card> cardsToScore) {
      return cardsToScore.stream().collect(Collectors.groupingBy(Card::getRank));
   }

}

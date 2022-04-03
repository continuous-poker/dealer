package de.doubleslash.poker.dealer.calculation.hands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Rank;

public class TwoPair implements PokerHand {

   @Override
   public int[] calculateScore(final List<Card> cardsToScore) {
      // [2,4-28,4-28,2-14]

      final TreeMap<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cardsToScore);

      final int pairScore1 = getAndRemoveHighestPair(cardsGroupedByRank);

      final int pairScore2 = getAndRemoveHighestPair(cardsGroupedByRank);

      final Card kicker = getHighestCard(cardsGroupedByRank);

      return IntStream.of(2, pairScore1, pairScore2, kicker.getValue()).toArray();

   }

   private Card getHighestCard(final TreeMap<Rank, List<Card>> cardsGroupedByRank) {
      return cardsGroupedByRank.firstEntry().getValue().get(0);
   }

   private int getAndRemoveHighestPair(final TreeMap<Rank, List<Card>> cardsGroupedByRank) {
      final Rank rank = cardsGroupedByRank.entrySet().stream().filter(e -> e.getValue().size() >= 2).map(Entry::getKey)
            .findFirst().orElseThrow(IllegalStateException::new);
      final int pairScore = rank.getValue() * 2;
      cardsGroupedByRank.remove(rank);
      return pairScore;
   }

   @Override
   public boolean matches(final List<Card> cardsToScore) {
      final Map<Rank, List<Card>> cardsGroupedByRank = getCardsGroupedByRank(cardsToScore);

      return cardsGroupedByRank.values().stream().filter(list -> list.size() >= 2).count() >= 2;
   }

   private TreeMap<Rank, List<Card>> getCardsGroupedByRank(final List<Card> cardsToScore) {
      final Map<Rank, List<Card>> cardsGroupedByRank = cardsToScore.stream()
            .collect(Collectors.groupingBy(Card::getRank));
      return new TreeMap<>(cardsGroupedByRank);
   }

}

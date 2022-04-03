package de.doubleslash.poker.dealer.calculation.hands;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Suit;

public class Flush implements PokerHand {

   @Override
   public int[] calculateScore(final List<Card> cardsToScore) {
      // [5,2-14,2-14,2-14,2-14,2-14]
      final Map<Suit, List<Card>> cardsGroupedBySuit = getCardsGroupedBySuit(cardsToScore);
      final List<Card> flush = getFlushCards(cardsGroupedBySuit);
      Collections.sort(flush);

      while (flush.size() > 5) {
         flush.remove(flush.size() - 1);
      }

      return IntStream.concat(IntStream.of(5), flush.stream().mapToInt(Card::getValue)).toArray();
   }

   private List<Card> getFlushCards(final Map<Suit, List<Card>> cardsGroupedBySuit) {
      final Optional<List<Card>> findFirst = cardsGroupedBySuit.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 5).map(Entry::getValue).findFirst();
      return findFirst.orElseThrow(IllegalStateException::new);
   }

   @Override
   public boolean matches(final List<Card> cardsToScore) {
      final Map<Suit, List<Card>> collect = getCardsGroupedBySuit(cardsToScore);
      return collect.values().stream().anyMatch(list -> list.size() >= 5);
   }

   private Map<Suit, List<Card>> getCardsGroupedBySuit(final List<Card> cardsToScore) {
      return cardsToScore.stream().collect(Collectors.groupingBy(Card::getSuit));
   }

}

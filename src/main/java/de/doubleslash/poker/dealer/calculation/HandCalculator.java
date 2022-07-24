package de.doubleslash.poker.dealer.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.doubleslash.poker.dealer.calculation.hands.Flush;
import de.doubleslash.poker.dealer.calculation.hands.FourOfAKind;
import de.doubleslash.poker.dealer.calculation.hands.FullHouse;
import de.doubleslash.poker.dealer.calculation.hands.HighCard;
import de.doubleslash.poker.dealer.calculation.hands.Pair;
import de.doubleslash.poker.dealer.calculation.hands.PokerHand;
import de.doubleslash.poker.dealer.calculation.hands.RoyalFlush;
import de.doubleslash.poker.dealer.calculation.hands.Straight;
import de.doubleslash.poker.dealer.calculation.hands.StraightFlush;
import de.doubleslash.poker.dealer.calculation.hands.ThreeOfAKind;
import de.doubleslash.poker.dealer.calculation.hands.TwoPair;
import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Player;

public class HandCalculator {

    List<PokerHand> hands;

    public HandCalculator() {
        hands = new ArrayList<>();

        hands.add(new RoyalFlush());
        hands.add(new StraightFlush());
        hands.add(new FourOfAKind());
        hands.add(new FullHouse());
        hands.add(new Flush());
        hands.add(new Straight());
        hands.add(new ThreeOfAKind());
        hands.add(new TwoPair());
        hands.add(new Pair());
        hands.add(new HighCard());
    }

    public Map<int[], List<Player>> determineWinningHand(final List<Player> players, final List<Card> communityCards) {

        final TreeMap<int[], List<Player>> playerScores = new TreeMap<>(new ScoreComparator());
        for (final Player player : players) {
            final List<Card> cards = player.getCards();

            final List<Card> hand = new ArrayList<>();
            hand.addAll(cards);
            hand.addAll(communityCards);

            for (final PokerHand calc : hands) {
                if (calc.matches(Collections.unmodifiableList(hand))) {
                    final int[] score = calc.calculateScore(hand);
                    playerScores.computeIfAbsent(score, k -> new ArrayList<>()).add(player);
                    break;
                }
            }
        }
        return playerScores;
    }

}

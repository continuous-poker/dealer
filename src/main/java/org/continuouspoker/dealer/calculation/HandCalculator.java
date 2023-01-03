package org.continuouspoker.dealer.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.continuouspoker.dealer.calculation.hands.Flush;
import org.continuouspoker.dealer.calculation.hands.FourOfAKind;
import org.continuouspoker.dealer.calculation.hands.FullHouse;
import org.continuouspoker.dealer.calculation.hands.HighCard;
import org.continuouspoker.dealer.calculation.hands.Pair;
import org.continuouspoker.dealer.calculation.hands.PokerHand;
import org.continuouspoker.dealer.calculation.hands.RoyalFlush;
import org.continuouspoker.dealer.calculation.hands.Straight;
import org.continuouspoker.dealer.calculation.hands.StraightFlush;
import org.continuouspoker.dealer.calculation.hands.ThreeOfAKind;
import org.continuouspoker.dealer.calculation.hands.TwoPair;
import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Player;

public class HandCalculator {

    private final List<PokerHand> hands;

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
        final List<Card> hand = new ArrayList<>();
        for (final Player player : players) {
            final List<Card> cards = player.getCards();
            hand.clear();
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

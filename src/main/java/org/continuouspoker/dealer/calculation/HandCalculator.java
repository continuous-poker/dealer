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
import org.continuouspoker.dealer.calculation.hands.Score;
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

    public Map<Score, List<Player>> determineWinningHand(final List<Player> players, final List<Card> communityCards) {

        final TreeMap<Score, List<Player>> playerScores = new TreeMap<>(new ScoreComparator());
        final List<Card> hand = new ArrayList<>();
        for (final Player player : players) {
            final List<Card> cards = player.getCards();
            hand.clear();
            hand.addAll(cards);
            hand.addAll(communityCards);

            for (final PokerHand calc : hands) {
                if (calc.matches(Collections.unmodifiableList(hand))) {
                    final Score score = calc.calculateScore(hand);


                    playerScores.computeIfAbsent(score, k -> new ArrayList<>()).add(player);
                    break;
                }
            }
        }
        return playerScores;
    }



}

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

import static org.continuouspoker.dealer.data.Rank.ACE;
import static org.continuouspoker.dealer.data.Rank.JACK;
import static org.continuouspoker.dealer.data.Rank.KING;
import static org.continuouspoker.dealer.data.Rank.NINE;
import static org.continuouspoker.dealer.data.Rank.QUEEN;
import static org.continuouspoker.dealer.data.Rank.TEN;
import static org.continuouspoker.dealer.data.Rank.THREE;
import static org.continuouspoker.dealer.data.Suit.CLUBS;
import static org.continuouspoker.dealer.data.Suit.HEARTS;
import static org.continuouspoker.dealer.data.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.continuouspoker.dealer.data.Card;
import org.junit.jupiter.api.Test;

class RoyalFlushTest {

    private final RoyalFlush strategy = new RoyalFlush();

    @Test
    void testCalculate_withLongStraight() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, CLUBS), new Card(QUEEN, SPADES),
                new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));
        Collections.shuffle(cards);

        final Score score = strategy.calculateScore(cards);

        assertArrayEquals(new int[] { 9
        }, score.scoreRank());
    }

    @Test
    void testMatches_doesMatch() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, SPADES),
                new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));

        assertTrue(strategy.matches(cards));
    }


    @Test
    void testMatches_doesNotMatch() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, HEARTS),
                new Card(JACK, SPADES), new Card(TEN, SPADES), new Card(NINE, SPADES), new Card(THREE, HEARTS));

        assertFalse(strategy.matches(cards));
    }

    @Test
    void testMatches_missingTen() {
        final List<Card> cards = Arrays.asList(new Card(KING, SPADES), new Card(ACE, SPADES), new Card(QUEEN, SPADES),
                new Card(JACK, SPADES), new Card(TEN, HEARTS), new Card(NINE, SPADES), new Card(THREE, HEARTS));

        assertFalse(strategy.matches(cards));
    }

}

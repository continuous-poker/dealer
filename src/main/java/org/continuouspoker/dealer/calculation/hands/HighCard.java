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
package org.continuouspoker.dealer.calculation.hands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.continuouspoker.dealer.data.Card;

public class HighCard implements PokerHand {

    @Override
    public Score calculateScore(final List<Card> cardsToScore) {
        final List<Card> cards = new ArrayList<>(cardsToScore);
        Collections.sort(cards);
        // [0,2-14,2-14,2-14,2-14,2-14]

        // just drop the lowest two cards
        cards.remove(cards.size() - 1);
        cards.remove(cards.size() - 1);

        return new Score("High Card",
                IntStream.concat(IntStream.of(0), cards.stream().mapToInt(Card::getValue)).toArray());
    }

    @Override
    public boolean matches(final List<Card> cardsToScore) {
        return true;
    }

}

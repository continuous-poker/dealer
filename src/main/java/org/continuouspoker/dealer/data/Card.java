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
package org.continuouspoker.dealer.data;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.text.WordUtils;

@Data
public class Card implements Comparable<Card>, Serializable {

    @Serial
    private static final long serialVersionUID = -6213516036380647535L;

    private final Rank rank;
    private final Suit suit;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(rank.name()) + " of " + WordUtils.capitalizeFully(suit.name());
    }

    @Override
    public int compareTo(final Card other) {
        return rank.compareTo(other.getRank());
    }

    @JsonIgnore
    public int getValue() {
        return rank.getValue();
    }

}

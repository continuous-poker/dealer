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

import java.util.Comparator;

import org.continuouspoker.dealer.calculation.hands.Score;

public class ScoreComparator implements Comparator<Score> {

    @Override
    public int compare(final Score playerScore1, final Score playerScore2) {
        final int[] array1 = playerScore1.scoreRank();
        final int[] array2 = playerScore2.scoreRank();
        final int maxLength = Math.min(array1.length, array2.length);

        for (int i = 0; i < maxLength; i++) {
            final int score1 = array1[i];
            final int score2 = array2[i];

            if (score1 != score2) {
                return Integer.compare(score1, score2) * -1;
            }
        }

        return 0;
    }

}

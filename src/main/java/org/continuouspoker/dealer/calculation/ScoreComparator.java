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

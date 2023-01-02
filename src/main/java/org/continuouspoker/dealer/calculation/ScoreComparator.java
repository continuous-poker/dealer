package org.continuouspoker.dealer.calculation;

import java.util.Comparator;

public class ScoreComparator implements Comparator<int[]> {

    @Override
    public int compare(final int[] array1, final int[] array2) {
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

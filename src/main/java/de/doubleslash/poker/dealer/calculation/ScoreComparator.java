package de.doubleslash.poker.dealer.calculation;

import java.util.Comparator;

public class ScoreComparator implements Comparator<int[]> {

   @Override
   public int compare(final int[] o1, final int[] o2) {
      final int maxLength = Math.min(o1.length, o2.length);

      for (int i = 0; i < maxLength; i++) {
         final int score1 = o1[i];
         final int score2 = o2[i];

         if (score1 != score2) {
            return Integer.compare(score1, score2) * -1;
         }
      }

      return 0;
   }

}

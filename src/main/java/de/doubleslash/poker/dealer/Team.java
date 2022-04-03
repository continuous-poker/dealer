package de.doubleslash.poker.dealer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Team {

   private final String name;
   private final ActionProvider provider;

   @Setter(AccessLevel.NONE)
   private long score = 0;

   public void addToScore(final long points) {
      score += points;
   }

}

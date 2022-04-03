package de.doubleslash.poker.dealer.data;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Suit {
   HEARTS,
   SPADES,
   CLUBS,
   DIAMONDS;

   @JsonCreator
   public static Suit forName(final String name) {
      return Arrays.stream(Suit.values())
                   .filter(s -> s.name()
                                 .equalsIgnoreCase(name))
                   .findFirst()
                   .orElseThrow(IllegalArgumentException::new);
   }
}

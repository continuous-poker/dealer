package de.doubleslash.poker.dealer.data;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Rank {
   ACE("A", 14),
   KING("K", 13),
   QUEEN("Q", 12),
   JACK("J", 11),
   TEN("10", 10),
   NINE("9", 9),
   EIGHT("8", 8),
   SEVEN("7", 7),
   SIX("6", 6),
   FIVE("5", 5),
   FOUR("4", 4),
   THREE("3", 3),
   TWO("2", 2);

   private String token;

   private int value;

   Rank(final String token, final int value) {
      this.token = token;
      this.value = value;
   }

   @JsonValue
   public String getToken() {
      return token;
   }

   @JsonCreator
   public static Rank forToken(final String token) {
      return Arrays.stream(Rank.values()).filter(r -> r.getToken().equals(token)).findFirst()
            .orElseThrow(IllegalArgumentException::new);
   }

   public int getValue() {
      return value;
   }

}

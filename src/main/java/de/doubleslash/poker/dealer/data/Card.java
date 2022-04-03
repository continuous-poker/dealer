package de.doubleslash.poker.dealer.data;

import java.io.Serializable;

import org.apache.commons.text.WordUtils;

import lombok.Data;

@Data
public class Card implements Comparable<Card>, Serializable {

   private static final long serialVersionUID = -6213516036380647535L;

   private final Rank rank;
   private final Suit suit;

   @Override
   public String toString() {
      return WordUtils.capitalizeFully(rank.name()) + " of " + WordUtils.capitalizeFully(suit.name());
   }

   @Override
   public int compareTo(final Card o) {
      return rank.compareTo(o.getRank());
   }

   public int getValue() {
      return rank.getValue();
   }

}

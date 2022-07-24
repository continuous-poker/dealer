package de.doubleslash.poker.dealer.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.text.WordUtils;

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

    @JsonIgnore
    public int getValue() {
        return rank.getValue();
    }

}

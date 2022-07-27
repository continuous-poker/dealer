package de.doubleslash.poker.dealer.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.doubleslash.poker.dealer.ActionProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@AllArgsConstructor
@Slf4j
public class Player implements CardReceiver, Serializable {
    private static final long serialVersionUID = -8077734808775553430L;

    private final String name;
    private Status status;
    private int stack;
    private int bet;
    private final List<Card> cards = new ArrayList<>();

    @JsonIgnore
    private final transient ActionProvider actionProvider;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public int bet(final int chips) {
        if (chips > stack) {
            bet = stack;
        } else if (chips > bet) {
            bet = chips;
        }
        return chips;
    }

    @Override
    public void takeCard(final Card card) {
        cards.add(card);
    }

    public void fold() {
        this.status = Status.FOLDED;
    }

    public int collectBet() {
        final int chips = bet;
        bet = 0;
        stack -= chips;
        log.info("Player {} stack: {}", name, stack);
        return chips;
    }

    public void addToStack(final int chips) {
        if (chips < 0) {
            throw new IllegalArgumentException();
        }
        stack += chips;
    }

    public boolean isAllIn() {
        return status.equals(Status.ACTIVE) && bet == stack;
    }

    public boolean isGoingAllIn(final int potentialBet) {
        return status.equals(Status.ACTIVE) && bet + potentialBet >= stack;
    }

    public void out() {
        this.status = Status.OUT;
    }

    public void active() {
        this.status = Status.ACTIVE;
    }

    public void clearCards() {
        cards.clear();
    }
}

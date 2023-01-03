package org.continuouspoker.dealer.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {
    @SuppressWarnings("PMD.LooseCoupling")
    private final LinkedList<Card> cards;

    public Deck(final List<Card> cards) {
        this.cards = new LinkedList<>(cards);
    }

    public Deck() {
        cards = new LinkedList<>();
        for (final Suit suit : Suit.values()) {
            for (final Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(cards);
    }

    public void burnCard() {
        cards.pop();
    }

    public void dealCards(final List<? extends CardReceiver> receivers, final int count) {
        for (int i = 0; i < count; i++) {
            for (final CardReceiver receiver : receivers) {
                final Card card = cards.pop();
                receiver.takeCard(card);
            }
        }
    }

    public void dealCards(final CardReceiver receiver, final int count) {
        dealCards(Collections.singletonList(receiver), count);
    }

}

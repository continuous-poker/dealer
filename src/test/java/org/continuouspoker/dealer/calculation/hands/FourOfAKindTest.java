package org.continuouspoker.dealer.calculation.hands;

import static org.continuouspoker.dealer.data.Rank.ACE;
import static org.continuouspoker.dealer.data.Rank.FIVE;
import static org.continuouspoker.dealer.data.Suit.CLUBS;
import static org.continuouspoker.dealer.data.Suit.DIAMONDS;
import static org.continuouspoker.dealer.data.Suit.HEARTS;
import static org.continuouspoker.dealer.data.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.continuouspoker.dealer.data.Card;
import org.junit.jupiter.api.Test;

class FourOfAKindTest {

    private final FourOfAKind fourOfAKind = new FourOfAKind();

    @Test
    void testCalculateFourOfAKind() throws Exception {
        final List<Card> cards = Arrays.asList(new Card(ACE, SPADES), new Card(FIVE, CLUBS), new Card(FIVE, HEARTS),
                new Card(ACE, CLUBS), new Card(ACE, DIAMONDS), new Card(FIVE, SPADES), new Card(ACE, HEARTS));
        Collections.shuffle(cards);

        final Score score = fourOfAKind.calculateScore(cards);

        assertArrayEquals(new int[] { 7,
                                      56,
                                      5
        }, score.scoreRank());
    }
}

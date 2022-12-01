package de.doubleslash.poker.dealer.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.data.Card;
import de.doubleslash.poker.dealer.data.Deck;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Rank;
import de.doubleslash.poker.dealer.data.Status;
import de.doubleslash.poker.dealer.data.Suit;
import de.doubleslash.poker.dealer.data.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameRoundTest {

    private GameLogger logger;
    private List<Player> players;
    private Table table;
    private GameRound testee;
    private Player player1;
    private Player player2;

    @BeforeEach
    public void setUp() {
        logger = mock(GameLogger.class);
        player1 = new Player("player1", Status.ACTIVE, 100, 0, table -> Integer.MAX_VALUE);
        player2 = new Player("player2", Status.ACTIVE, 100, 0, table -> Integer.MAX_VALUE);
        players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        table = new Table(1, players, 5, msg -> logger.log(1, 1,0, msg, null));
        testee = new GameRound(players, table, logger, 1);

    }

    @Test
    void testPlayWithDeck() throws Exception {
        final List<Card> cards = new ArrayList<>();
        fillCards(cards);
        final Deck deck = new Deck(cards);

        testee.playWithDeck(deck);

        assertEquals(0, player1.getStack());
        assertEquals(200, player2.getStack());
    }

    private void fillCards(final List<Card> cards) {
        cards.add(new Card(Rank.ACE, Suit.CLUBS));
        cards.add(new Card(Rank.QUEEN, Suit.CLUBS));
        cards.add(new Card(Rank.ACE, Suit.DIAMONDS));
        cards.add(new Card(Rank.QUEEN, Suit.DIAMONDS));

        cards.add(new Card(Rank.SEVEN, Suit.DIAMONDS));

        cards.add(new Card(Rank.JACK, Suit.SPADES));
        cards.add(new Card(Rank.THREE, Suit.HEARTS));
        cards.add(new Card(Rank.KING, Suit.HEARTS));

        cards.add(new Card(Rank.FOUR, Suit.CLUBS));

        cards.add(new Card(Rank.THREE, Suit.CLUBS));

        cards.add(new Card(Rank.TEN, Suit.HEARTS));

        cards.add(new Card(Rank.FOUR, Suit.DIAMONDS));

        cards.add(new Card(Rank.FOUR, Suit.HEARTS));

    }

}

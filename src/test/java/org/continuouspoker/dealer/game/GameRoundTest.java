/*
 * Copyright © 2020 - 2024 Jan Kreutzfeld
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.continuouspoker.dealer.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.continuouspoker.dealer.ActionProvider;
import org.continuouspoker.dealer.data.Card;
import org.continuouspoker.dealer.data.Deck;
import org.continuouspoker.dealer.data.Player;
import org.continuouspoker.dealer.data.Rank;
import org.continuouspoker.dealer.data.Status;
import org.continuouspoker.dealer.data.Suit;
import org.continuouspoker.dealer.data.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameRoundTest {

    private List<Player> players;
    private GameRound testee;

    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    public void setUp() {
        ActionProvider actionProvider1 = mock(ActionProvider.class);
        ActionProvider actionProvider2 = mock(ActionProvider.class);

        player1 = new Player("player1", Status.ACTIVE, 100, 0, actionProvider1);
        player2 = new Player("player2", Status.ACTIVE, 100, 0, actionProvider2);
        player3 = new Player("player3", Status.ACTIVE, 100, 0, actionProvider2);

        players = new ArrayList<>();

        Table table = new Table(1, players, 5);

        testee = new GameRound(1, players, table, Duration.ZERO);
    }

    @Test
    void test_BothAllIn() throws Exception {
        players.add(player1);
        players.add(player2);

        final List<Card> cards = new ArrayList<>();
        fillCards(cards, 2);
        final Deck deck = new Deck(cards);

        when(player1.getActionProvider().requestBet(any(), any())).thenReturn(100);
        when(player2.getActionProvider().requestBet(any(), any())).thenReturn(100);

        testee.playWithDeck(deck);

        assertEquals(0, player1.getStack());
        assertEquals(200, player2.getStack());
    }

    @Test
    void testNoWinner() throws Exception {
        players.add(player1);
        players.add(player2);

        final List<Card> cards = new ArrayList<>();
        fillCards(cards, 2);

        cards.set(1, new Card(Rank.ACE, Suit.HEARTS));
        cards.set(3, new Card(Rank.ACE, Suit.SPADES));
        final Deck deck = new Deck(cards);

        when(player1.getActionProvider().requestBet(any(), any())).thenReturn(20);
        when(player2.getActionProvider().requestBet(any(), any())).thenReturn(20);

        testee.playWithDeck(deck);

        assertEquals(100, player1.getStack());
        assertEquals(100, player2.getStack());
    }

    @Test
    void testPlayer1AllIn_Round1_2Players_ButLose() throws Exception {
        players.add(player1); //stack: 100

        player2.addToStack(100);
        players.add(player2); // stack: 200

        final List<Card> cards = new ArrayList<>();
        fillCards(cards, 2);
        final Deck deck = new Deck(cards);

        when(player1.getActionProvider().requestBet(any(), any())).thenReturn(100);
        when(player2.getActionProvider().requestBet(any(), any())).thenReturn(100);

        testee.playWithDeck(deck);

        assertEquals(0, player1.getStack());
        assertEquals(300, player2.getStack());
    }

    @Test
    void testPlayer1AllIn_Round2_2Players_ButWin() throws Exception {
        players.add(player1); //stack: 100

        player2.addToStack(100);
        players.add(player2); // stack: 200

        final List<Card> cards = new ArrayList<>();
        fillCards(cards, 2);

        cards.set(0, new Card(Rank.QUEEN, Suit.CLUBS));
        cards.set(1, new Card(Rank.ACE, Suit.CLUBS));
        cards.set(2, new Card(Rank.QUEEN, Suit.DIAMONDS));
        cards.set(3, new Card(Rank.ACE, Suit.DIAMONDS));
        final Deck deck = new Deck(cards);

        when(player1.getActionProvider().requestBet(any(), any())).thenReturn(50);
        when(player2.getActionProvider().requestBet(any(), any())).thenReturn(50, 50);

        testee.playWithDeck(deck);

        assertEquals(Status.ACTIVE, player1.getStatus());
        assertEquals(200, player1.getStack());
        assertEquals(100, player2.getStack());
    }

    @Test
    void testPlayer1AllIn_Round3_3Players_ButWin() throws Exception {
        player1.addToStack(50);
        players.add(player1); //stack: 150

        player2.addToStack(100);
        players.add(player2); // stack: 200

        player3.addToStack(100);
        players.add(player3);// stack: 200

        final List<Card> cards = new ArrayList<>();
        fillCards(cards, 3);

        final Deck deck = new Deck(cards);

        when(player1.getActionProvider().requestBet(any(), any())).thenReturn(50);
        when(player2.getActionProvider().requestBet(any(), any())).thenReturn(50, 50);
        when(player3.getActionProvider().requestBet(any(), any())).thenReturn(50, 50);

        testee.playWithDeck(deck);

        assertEquals(550, player1.getStack());
        assertEquals(0, player2.getStack());
        assertEquals(0, player3.getStack());
    }

    @Test
    void testPlayer1AllIn_Round4_2Players_ButLose() throws Exception {
        players.add(player1); //stack: 100

        player2.addToStack(100);
        players.add(player2); // stack: 200

        final List<Card> cards = new ArrayList<>();
        fillCards(cards, 2);
        final Deck deck = new Deck(cards);

        when(player1.getActionProvider().requestBet(any(), any())).thenReturn(25);
        when(player2.getActionProvider().requestBet(any(), any())).thenReturn(25);

        testee.playWithDeck(deck);

        assertEquals(0, player1.getStack());
        assertEquals(300, player2.getStack());
    }

    private void fillCards(final List<Card> cards, final int playerCount) {

        switch (playerCount) {
            case 2: {
                cards.add(new Card(Rank.ACE, Suit.CLUBS));      //player2
                cards.add(new Card(Rank.QUEEN, Suit.CLUBS));    //player1

                cards.add(new Card(Rank.ACE, Suit.DIAMONDS));   //player2
                cards.add(new Card(Rank.QUEEN, Suit.DIAMONDS)); //player1
                break;
            }
            case 3: {
                cards.add(new Card(Rank.QUEEN, Suit.CLUBS));    //player2
                cards.add(new Card(Rank.FIVE, Suit.HEARTS));    //player3
                cards.add(new Card(Rank.ACE, Suit.CLUBS));      //player1

                cards.add(new Card(Rank.QUEEN, Suit.DIAMONDS)); //player2
                cards.add(new Card(Rank.TWO, Suit.HEARTS));     //player3
                cards.add(new Card(Rank.ACE, Suit.DIAMONDS));   //player1
                break;
            }
            case 4: {
                //add cards for 4 players according to the order above (expl. 2 3 4 1)
            }

            default: {
                System.err.println("fillCards for " + playerCount + " players not implemented");
                break;
            }
        }

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

package de.doubleslash.poker.dealer.game;

import java.util.List;
import java.util.Optional;

import de.doubleslash.poker.dealer.GameLogger;
import de.doubleslash.poker.dealer.data.Player;
import de.doubleslash.poker.dealer.data.Seats;
import de.doubleslash.poker.dealer.data.Table;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BetRound {
   private static final String ALL_IN_TEXT = "Player %s goes all in with %s.";

   private final Table table;
   private final List<Player> playersInPlayOrder;
   private final boolean isPreFlop;
   private final GameLogger logger;
   private final long gameId;

   public BetRound(final long gameId, final Table table, final List<Player> playersInPlayOrder, final boolean isPreFlop,
         final GameLogger logger) {
      this.gameId = gameId;
      this.table = table;
      this.playersInPlayOrder = playersInPlayOrder;
      this.isPreFlop = isPreFlop;
      this.logger = logger;
   }

   public Optional<Player> run() {
      logger.log(gameId, table.getId(), "Starting bet round.");
      final Seats seats = new Seats(playersInPlayOrder);
      Player lastBettingPlayer = seats.getCurrentPlayer();
      int lastBet = 0;

      playersInPlayOrder.forEach(System.out::println);

      if (isPreFlop) {
         lastBettingPlayer = collectBlinds(table, seats);
         lastBet = lastBettingPlayer.getBet();
         table.setMinimumBet(table.getSmallBlind() * 2);
      }

      Player currentPlayer;
      while ((currentPlayer = seats.getNextActivePlayer()) != null) {
         table.setActivePlayer(currentPlayer);
         // check for only one left -> he wins
         if (seats.getNumberOfActivePlayers() == 1) {
            // we have a winner
            logger.log(gameId, table.getId(), "Ending bet round with winner: %s", currentPlayer.getName());

            return Optional.of(currentPlayer);
         }

         if (currentPlayer.isAllIn()) {
            if (currentPlayer.equals(lastBettingPlayer)) {
               break;
            } else {
               continue;
            }
         }

         if (currentPlayer.equals(lastBettingPlayer)) {
            if (lastBet == 0 || (isPreFlop && lastBet <= table.getSmallBlind() * 2)) {
               // nobody bet / raised and we are at the starting player again
               // let him bet / raise or check
               callPlayer(table, currentPlayer);
               if (currentPlayer.getBet() < table.getMinimumRaise()) {
                  // he checked
                  break;
               } else {
                  // he bet / raised
                  lastBet = currentPlayer.getBet();
                  table.setMinimumBet(lastBet);
                  lastBettingPlayer = currentPlayer;
               }
            } else {
               break;
            }
         } else {
            callPlayer(table, currentPlayer);
            if (currentPlayer.getBet() > lastBet) {
               lastBet = currentPlayer.getBet();
               table.setMinimumBet(lastBet);
               lastBettingPlayer = currentPlayer;
            }
         }
      }
      logger.log(gameId, table.getId(), "Ending bet round.");
      return Optional.empty();

   }

   private Player collectBlinds(final Table table, final Seats seats) {
      Player lastBettingPlayer;
      // collect blinds
      final Player small = seats.getCurrentPlayer();
      small.bet(table.getSmallBlind());

      if (small.isAllIn()) {
         logger.log(gameId, table.getId(), "Player %s goes all in for small blind with %s.", small.getName(),
               small.getBet());
      } else {
         log.info("{} pays small blind of {}", small.getName(), small.getBet());
      }

      final Player big = seats.getNextActivePlayer();
      big.bet(table.getSmallBlind() * 2);

      if (big.isAllIn()) {
         logger.log(gameId, table.getId(), "Player %s goes all in for big blind with %s.", big.getName(), big.getBet());
      } else {
         log.info("{} pays big blind of {}", big.getName(), big.getBet());
      }

      lastBettingPlayer = big;
      return lastBettingPlayer;
   }

   private void callPlayer(final Table table, final Player player) {
      int result = player.getActionProvider()
                         .requestBet(table.copyForActivePlayer());

      if (result < player.getBet()) {
         result = player.getBet();
      }

      if (result < table.getMinimumBet()) {
         player.fold();
         logger.log(gameId, table.getId(), "Player %s folds.", player.getName());

      } else if (result >= table.getMinimumBet() && result < table.getMinimumRaise()) {
         // not enough for a raise, just a call
         player.bet(table.getMinimumBet());
         logger.log(gameId, table.getId(), "Player %s calls.", player.getName());
      } else {
         player.bet(result);
         if (player.isAllIn()) {
            logger.log(gameId, table.getId(), ALL_IN_TEXT, player.getName(), player.getBet());
         } else {
            logger.log(gameId, table.getId(), "Player %s raises to %s.", player.getName(), result);
         }
      }
   }

}

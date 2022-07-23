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

      if (isPreFlop) {
         lastBettingPlayer = collectBlinds(table, seats);
         lastBet = lastBettingPlayer.getBet();
      }
      table.setMinimumBet(table.getSmallBlind() * 2);

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
            if (lastBet == 0 || (isPreFlop && lastBet == table.getSmallBlind() * 2)) {
               // nobody bet / raised and we are at the starting player again
               // let him bet / raise or check
               callPlayer(table, currentPlayer);
               if (currentPlayer.getBet() <= table.getMinimumBet()) {
                  // he checked
                  break;
               } else {
                  // he bet / raised
                  lastBet = currentPlayer.getBet();
                  table.setMinimumBet(lastBet);
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
         logger.log(gameId, table.getId(), "Player %s pays small blind of %s.", small.getName(),
                 small.getBet());
         log.info("{} pays small blind of {}", small.getName(), small.getBet());
      }

      final Player big = seats.getNextActivePlayer();
      big.bet(table.getSmallBlind() * 2);

      if (big.isAllIn()) {
         logger.log(gameId, table.getId(), "Player %s goes all in for big blind with %s.", big.getName(), big.getBet());
      } else {
         logger.log(gameId, table.getId(), "Player %s pays big blind of %s.", big.getName(),
                 big.getBet());
         log.info("{} pays big blind of {}", big.getName(), big.getBet());
      }

      lastBettingPlayer = big;
      return lastBettingPlayer;
   }

   private void callPlayer(final Table table, final Player player) {
      log.debug("Calling player {} with table {}", player.getName(), table);
      int result = player.getActionProvider()
                         .requestBet(table.copyForActivePlayer());
      log.debug("Player {} returned bet of {}", player.getName(), result);

      if (result < player.getBet()) {
         log.debug("Result {} is lower than current bet of {} for player {}, setting it to the current bet.",
                 result, player.getBet(), player.getName());
         result = player.getBet();
      }

      if (result < table.getMinimumBet()) {
         if (noPlayerHasBetYet(table)) {
            player.bet(0);
            logger.log(gameId, table.getId(), "Player %s checks.", player.getName());
         } else {
            player.fold();
            logger.log(gameId, table.getId(), "Player %s folds.", player.getName());
         }
      } else if (result >= table.getMinimumBet() && result < table.getMinimumRaise()) {
         if (noPlayerHasBetYet(table)) {
            logger.log(gameId, table.getId(), "Player %s bets %s.", player.getName(), result);
         } else {
            // not enough for a raise, just a call
            logger.log(gameId, table.getId(), "Player %s calls the bet of %s.", player.getName(), table.getMinimumBet());
            player.bet(table.getMinimumBet());
         }
      } else {
         player.bet(result);
         if (player.isAllIn()) {
            logger.log(gameId, table.getId(), ALL_IN_TEXT, player.getName(), player.getBet());
         } else {
            if (noPlayerHasBetYet(table)) {
               logger.log(gameId, table.getId(), "Player %s bets %s.", player.getName(), result);
            } else {
               logger.log(gameId, table.getId(), "Player %s raises to %s.", player.getName(), result);
            }
         }
      }
   }

   private boolean noPlayerHasBetYet(final Table table) {
      return table.getPlayers().stream().mapToInt(Player::getBet).sum() == 0;
   }

}

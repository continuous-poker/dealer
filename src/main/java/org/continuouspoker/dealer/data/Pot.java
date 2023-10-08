package org.continuouspoker.dealer.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.continuouspoker.dealer.StepLogger;
import org.continuouspoker.dealer.calculation.hands.Score;

@Slf4j
public class Pot implements Serializable {

    private final transient StepLogger stepLogger;
    private final List<PotPart> pots = new ArrayList<>();

    public Pot(final StepLogger logger) {
        this.stepLogger = logger;
        reset();
    }

    public int getTotalSize() {
        return pots.stream().mapToInt(PotPart::getSize).sum();
    }

    private void reset() {
        pots.clear();
        final PotPart mainPot = new PotPart();
        pots.add(mainPot);
    }

    public void pay(final Player winner) {
        pots.forEach(p -> log.info(p.toString()));
        stepLogger.log("All pots go to " + winner.getName() + " with " + getTotalSize() + " chips in total.");
        pots.forEach(pot -> winner.addToStack(pot.getSize()));
        reset();
    }

    public void pay(final Map<Score, List<Player>> rankedPlayers) {
        rankedPlayers.forEach((score, players) -> {
            final Iterator<PotPart> iterator = pots.iterator();
            final HashSet<Player> winners = new HashSet<>();
            while (iterator.hasNext()) {
                final PotPart pot = iterator.next();
                final int potSize = pot.getSize();
                final Collection<Player> payees = pot.getPayees();
                winners.clear();
                winners.addAll(players);
                winners.retainAll(payees);
                if (!winners.isEmpty()) {
                    final int split = potSize / winners.size();
                    winners.forEach(p -> p.addToStack(split));

                    final String winnerString = winners.stream().map(Player::getName).collect(Collectors.joining(","));
                    log.info("Winners: {} ({} each) with a {}", winnerString, split, score.name());

                    if (moreThanOneWinner(winners)) {
                        stepLogger.log(String.format("Pot of %s is split between %s (%s for each), for a '%s'",
                                potSize, winnerString, split, score.name()));
                    } else {
                        stepLogger.log(String.format("Pot of %s goes to %s, for a '%s'", potSize, winnerString,
                                score.name()));
                    }

                    iterator.remove();
                }
            }
        });

        reset();
    }

    private static boolean moreThanOneWinner(final Collection<Player> winners) {
        return winners.size() > 1;
    }

    public void collect(final List<Player> playersInPlayOrder) {
        final List<Player> players = sortPlayersByCurrentBet(playersInPlayOrder);

        final PotPart startPot = pots.get(pots.size() - 1);

        pots.forEach(PotPart::resetBetLimit);

        players.forEach(p -> collectFromPlayer(startPot, p));

    }

    private void collectFromPlayer(final PotPart startPot, final Player player) {
        int remainingBet = player.collectBet();

        boolean skip = true;
        for (final PotPart pot : pots) {
            if (pot.equals(startPot)) {
                skip = false;
            }
            if (skip) {
                continue;
            }

            remainingBet = addToPot(player, remainingBet, pot);
        }
        if (remainingBet > 0) {
            final PotPart pot = new PotPart();
            pot.addPayee(player);
            pot.add(remainingBet);
            if (player.isAllIn()) {
                pot.setBetLimit(remainingBet);
            }
            pots.add(pot);
        }
    }

    private static int addToPot(final Player player, final int bet, final PotPart pot) {
        final int betLimit = pot.getBetLimit();
        pot.addPayee(player);
        if (betLimit == 0) {
            pot.add(bet);
            if (player.isAllIn()) {
                pot.setBetLimit(bet);
            }
            return 0;
        } else {
            pot.add(betLimit);
            return bet - betLimit;
        }
    }

    private static List<Player> sortPlayersByCurrentBet(final List<Player> players) {
        final ArrayList<Player> localPlayers = new ArrayList<>(players);
        localPlayers.sort(Comparator.comparingInt(Player::getCurrentBet));
        return localPlayers;
    }

    @Override
    public String toString() {
        return pots.stream().map(PotPart::toString).collect(Collectors.joining(","));
    }

    private static final class PotPart implements Serializable {

        private int size;
        private final Set<Player> payees = new HashSet<>();

        private int betLimit;

        public int getSize() {
            return size;
        }

        public Set<Player> getPayees() {
            return payees;
        }

        public void add(final int chips) {
            size += chips;
        }

        public void addPayee(final Player player) {
            payees.add(player);
        }

        public void resetBetLimit() {
            betLimit = 0;
        }

        public int getBetLimit() {
            return betLimit;
        }

        public void setBetLimit(final int betLimit) {
            this.betLimit = betLimit;
        }

        @Override
        public String toString() {
            return String.format("Pot of %s (%s)", size,
                    payees.stream().map(Player::getName).collect(Collectors.joining(",")));
        }
    }
}

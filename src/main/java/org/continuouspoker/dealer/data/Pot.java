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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Pot implements Serializable {

    private final transient Consumer<String> logger;
    private final List<PotPart> pots = new ArrayList<>();

    public Pot(final Consumer<String> logger) {
        this.logger = logger;
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
        logger.accept("All pots go to " + winner.getName() + " with " + getTotalSize() + " chips in total.");
        pots.forEach(pot -> winner.addToStack(pot.getSize()));
        reset();
    }

    public void pay(final Map<int[], List<Player>> rankedPlayers) {
        final Collection<List<Player>> values = rankedPlayers.values();
        values.forEach(players -> {
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
                    log.info("Winners: {} ({} each)", winnerString, split);

                    if (moreThanOneWinner(winners)) {
                        logger.accept(
                                String.format("Pot of %s is split between %s (%s for each)", potSize, winnerString,
                                        split));
                    } else {
                        logger.accept(String.format("Pot of %s goes to %s", potSize, winnerString));
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
        final List<Player> players = new ArrayList<>(playersInPlayOrder);
        players.sort(Comparator.comparingInt(Player::getCurrentBet));

        final PotPart startPot = pots.get(pots.size() - 1);

        pots.forEach(PotPart::resetBetLimit);

        players.forEach(p -> {
            int bet = p.collectBet();

            boolean skip = true;
            for (final PotPart pot : pots) {
                if (pot.equals(startPot)) {
                    skip = false;
                }
                if (skip) {
                    continue;
                }

                final int betLimit = pot.getBetLimit();
                if (betLimit == 0) {
                    pot.add(bet);
                    if (p.isAllIn()) {
                        pot.setBetLimit(bet);
                    }
                    bet = 0;
                } else {
                    pot.add(betLimit);
                    bet -= betLimit;
                }

                pot.addPayee(p);
            }
            if (bet > 0) {
                final PotPart pot = new PotPart();
                pot.addPayee(p);
                pot.add(bet);
                pots.add(pot);
            }
        });

    }

    @Override
    public String toString() {
        return pots.stream().map(PotPart::toString).collect(Collectors.joining(","));
    }

    private static class PotPart implements Serializable {

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

package de.doubleslash.poker.dealer.data;

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

@Slf4j
public class Pot implements Serializable {

    public int getTotalSize() {
        return pots.stream().mapToInt(PotPart::getSize).sum();
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

        public void addPayee(final Player p) {
            payees.add(p);
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

    private final List<PotPart> pots = new ArrayList<>();

    public Pot() {
        reset();
    }

    private void reset() {
        pots.clear();
        final PotPart mainPot = new PotPart();
        pots.add(mainPot);
    }

    public void pay(final Player winner) {
        pots.forEach(p -> log.info(p.toString()));
        log.info("All pots go to {}", winner.getName());
        pots.forEach(pot -> winner.addToStack(pot.getSize()));
        reset();
    }

    public void pay(final Map<int[], List<Player>> rankedPlayers) {
        final Collection<List<Player>> values = rankedPlayers.values();
        values.forEach(players -> {
            final Iterator<PotPart> i = pots.iterator();
            while (i.hasNext()) {
                final PotPart pot = i.next();
                final int potSize = pot.getSize();
                final Collection<Player> payees = pot.getPayees();

                final HashSet<Player> winners = new HashSet<>(players);
                winners.retainAll(payees);
                if (!winners.isEmpty()) {
                    final int split = potSize / winners.size();
                    winners.forEach(p -> p.addToStack(split));

                    log.info("Winners: {} ({} each)",
                            winners.stream().map(Player::getName).collect(Collectors.joining(",")), split);

                    i.remove();
                }
            }
        });

        reset();
    }

    public void collect(final List<Player> playersInPlayOrder) {
        final List<Player> players = new ArrayList<>(playersInPlayOrder);
        players.sort(Comparator.comparingInt(Player::getBet));

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

}

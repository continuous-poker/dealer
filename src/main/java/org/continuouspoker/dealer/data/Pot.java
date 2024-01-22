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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
        final PotPart mainPot = new PotPart("Main pot");
        pots.add(mainPot);
    }

    public void pay(final Player winner) {
        pots.forEach(p -> log.info(p.toString()));
        stepLogger.log("All pots go to " + winner.getName() + " with " + getTotalSize() + " chips in total.");
        pots.forEach(pot -> winner.addToStack(pot.getSize()));
        reset();
    }

    public void pay(final Map<Score, List<Player>> rankedPlayers) {
        rankedPlayers.forEach(this::payRankedPlayers);
        reset();
    }

    private void payRankedPlayers(final Score score, final List<Player> players) {
        final Iterator<PotPart> iterator = pots.iterator();
        final HashSet<Player> winners = new HashSet<>();
        while (iterator.hasNext()) {
            final PotPart pot = iterator.next();
            final Collection<Player> payees = pot.getPayees();
            winners.clear();
            winners.addAll(players);
            winners.retainAll(payees);
            if (!winners.isEmpty()) {
                payPotToWinners(score, winners, pot);
                iterator.remove();
            }
        }
    }

    private void payPotToWinners(final Score score, final Set<Player> winners, final PotPart pot) {
        final int potSize = pot.getSize();
        final int split = potSize / winners.size();
        winners.forEach(p -> p.addToStack(split));

        final String winnerString = winners.stream().map(Player::getName).collect(Collectors.joining(","));
        log.info("Winners: {} ({} each) with a {}", winnerString, split, score.name());

        if (moreThanOneWinner(winners)) {
            stepLogger.log(
                    String.format("%s of %s is split between %s (%s for each), for a '%s'", pot.getName(), potSize,
                            winnerString, split, score.name()));
        } else {
            stepLogger.log(String.format("%s of %s goes to %s, for a '%s'", pot.getName(), potSize, winnerString,
                    score.name()));
        }

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
        if (remainingBet > 0) {
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
                final PotPart pot = new PotPart("Side pot " + pots.size());
                pot.addPayee(player);
                pot.add(remainingBet);
                if (player.isAllIn()) {
                    pot.setBetLimit(remainingBet);
                }
                pots.add(pot);
            }
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

    @RequiredArgsConstructor
    @Getter
    private static final class PotPart implements Serializable {

        private int size;
        private final Set<Player> payees = new HashSet<>();
        private final String name;

        @Setter
        private int betLimit;

        public void add(final int chips) {
            size += chips;
        }

        public void addPayee(final Player player) {
            payees.add(player);
        }

        public void resetBetLimit() {
            betLimit = 0;
        }

        @Override
        public String toString() {
            return String.format("%s of %s (%s)", name, size,
                    payees.stream().map(Player::getName).collect(Collectors.joining(",")));
        }
    }
}

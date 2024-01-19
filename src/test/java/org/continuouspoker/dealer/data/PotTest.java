/*
 * Copyright © 2024 DoubleSlash Net-Business GmbH
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.continuouspoker.dealer.calculation.ScoreComparator;
import org.continuouspoker.dealer.calculation.hands.Score;
import org.junit.jupiter.api.Test;

class PotTest {

    @Test
    void shouldProperlyDistributeSidePots() {
        final Pot pot = new Pot(System.out::println);

        final var p1 = createPlayer("P1", 20);
        final var p2 = createPlayer("P2", 100);
        final var p3 = createPlayer("P3", 200);
        final var p4 = createPlayer("P4", 200);
        final var players = List.of(p1, p2, p3, p4);

        p1.bet(20);
        p2.bet(100);
        p3.bet(150);
        p4.bet(150);

        pot.collect(players);

        final TreeMap<Score, List<Player>> winnerMap = new TreeMap<>(new ScoreComparator());
        winnerMap.putAll(Map.of(straightFlush(), List.of(p1), flush(), List.of(p2), pair(), List.of(p3, p4)));

        pot.pay(winnerMap);

        assertThat(p1.getStack()).isEqualTo(80);
        assertThat(p2.getStack()).isEqualTo(240);
        assertThat(p3.getStack()).isEqualTo(100);
        assertThat(p4.getStack()).isEqualTo(100);
    }

    @Test
    void shouldProperlyDistributeSidePotsWithLateAllIn() {
        final Pot pot = new Pot(System.out::println);

        final var p1 = createPlayer("P1", 80);
        final var p2 = createPlayer("P2", 120);
        final var p3 = createPlayer("P3", 20);
        final var p4 = createPlayer("P4", 200);
        final var players = List.of(p1, p2, p3, p4);

        p1.bet(80);
        p2.bet(120);
        p3.bet(20);
        p4.bet(120);

        pot.collect(players);

        final TreeMap<Score, List<Player>> winnerMap = new TreeMap<>(new ScoreComparator());
        winnerMap.putAll(Map.of(straightFlush(), List.of(p1), flush(), List.of(p2), pair(), List.of(p3, p4)));

        pot.pay(winnerMap);

        assertThat(p1.getStack()).isEqualTo(260);
        assertThat(p2.getStack()).isEqualTo(80);
        assertThat(p3.getStack()).isEqualTo(0);
        assertThat(p4.getStack()).isEqualTo(80);
    }

    @Test
    void shouldProperlyDistributeSidePotsWithMultipleBetRounds() {
        final Pot pot = new Pot(System.out::println);

        final var p1 = createPlayer("P1", 20);
        final var p2 = createPlayer("P2", 100);
        final var p3 = createPlayer("P3", 200);
        final var p4 = createPlayer("P4", 200);
        final var players = List.of(p1, p2, p3, p4);

        p1.bet(10);
        p2.bet(10);
        p3.bet(10);
        p4.bet(10);

        pot.collect(players);

        p1.bet(10);
        p2.bet(50);
        p3.bet(50);
        p4.bet(50);

        pot.collect(players);

        p2.bet(40);
        p3.bet(90);
        p4.bet(90);

        pot.collect(players);

        final TreeMap<Score, List<Player>> winnerMap = new TreeMap<>(new ScoreComparator());
        winnerMap.putAll(Map.of(straightFlush(), List.of(p1), flush(), List.of(p2), pair(), List.of(p3, p4)));

        pot.pay(winnerMap);

        assertThat(p1.getStack()).isEqualTo(80);
        assertThat(p2.getStack()).isEqualTo(240);
        assertThat(p3.getStack()).isEqualTo(100);
        assertThat(p4.getStack()).isEqualTo(100);
    }

    private Score pair() {
        return new Score("pair", new int[] { 1 });
    }

    private Score flush() {
        return new Score("flush", new int[] { 2 });
    }

    private Score straightFlush() {
        return new Score("straight flush", new int[] { 3 });
    }

    private Player createPlayer(final String name, final int stack) {
        return new Player(name, Status.ACTIVE, stack, 0, (table, logger) -> 0);
    }

}
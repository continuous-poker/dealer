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
package org.continuouspoker.dealer;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.continuouspoker.dealer.data.Table;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@Slf4j
public class RemotePlayer implements ActionProvider {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(1);
    private static final int MAX_STRIKES = 3;
    @JsonProperty
    private final String url;
    @JsonProperty
    private int strike;

    private long blockedTable = -1;

    public RemotePlayer(final String playerUrl) {
        this.url = toAbsolute(playerUrl);
        // test player URL format
        URI.create(this.url);
    }

    private String toAbsolute(final String playerUrl) {
        if (!playerUrl.startsWith("http")) {
            return "http://" + playerUrl;
        }
        return playerUrl;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public int requestBet(final Table table, final StepLogger logger) {
        if (table.getTournamentId() == blockedTable) {
            log.info("Player is blocked from this table, will instantly return a bet of 0.");
            logger.log("Player %s is blocked from this tournament and cannot bet.", getPlayerName(table));
            return 0;
        }

        try (var client = createClient()) {
            final int response = client.getExtensionsById(table).bet();
            strike = 0;
            return response;
        } catch (final Exception e) {
            log.error("Error while requesting bet from player {}", url, e);
            logger.log("Request to player %s failed or took too long - Strike %s", getPlayerName(table), strike + 1);
            addStrike(table);
            return 0;
        }
    }

    private static String getPlayerName(final Table table) {
        return table.getPlayers().get(table.getActivePlayer()).getName();
    }

    private RemotePlayerClient createClient() {
        return RestClientBuilder.newBuilder()
                                .baseUri(URI.create(this.url))
                                .connectTimeout(CONNECT_TIMEOUT.getSeconds(), TimeUnit.SECONDS)
                                .readTimeout(READ_TIMEOUT.getSeconds(), TimeUnit.SECONDS)
                                .build(RemotePlayerClient.class);
    }

    private void addStrike(final Table table) {
        strike++;
        if (strike == MAX_STRIKES) {
            blockedTable = table.getTournamentId();
            strike = 0;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}

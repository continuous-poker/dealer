package org.continuouspoker.dealer;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.continuouspoker.dealer.data.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@Slf4j
public class RemotePlayer implements ActionProvider {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    @JsonProperty
    private final String url;
    private final RemotePlayerClient client;

    @JsonProperty
    private int strike;

    private long blockedTable = -1;

    public RemotePlayer(final String playerUrl) {
        this.url = toAbsolute(playerUrl);
        client = RestClientBuilder.newBuilder()
                                  .baseUri(URI.create(this.url))
                                  .connectTimeout(CONNECT_TIMEOUT.getSeconds(), TimeUnit.SECONDS)
                                  .readTimeout(READ_TIMEOUT.getSeconds(), TimeUnit.SECONDS)
                                  .build(RemotePlayerClient.class);
    }

    private String toAbsolute(final String playerUrl) {
        if (!playerUrl.startsWith("http")) {
            return "http://" + playerUrl;
        }
        return playerUrl;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public int requestBet(final Table table) {
        if (table.getTournamentId() == blockedTable) {
            return 0;
        }

        try {
            final int response = client.getExtensionsById(table).bet();
            strike = 0;
            return response;
        } catch (final Exception e) {
            log.error("Error while requesting bet from player {}", url, e);
            addStrike(table);
            return 0;
        }
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

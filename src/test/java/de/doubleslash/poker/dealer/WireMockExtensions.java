package de.doubleslash.poker.dealer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {
    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        final String json = readString();
        wireMockServer.stubFor(post(urlEqualTo("/")).withRequestBody(equalToJson(json))
                                                    .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                                                           .withBody("{ \"bet\": 5 }")));

        return Collections.singletonMap("quarkus.rest-client.\"de.doubleslash.poker.dealer.RemotePlayerClient\".url",
                wireMockServer.baseUrl());
    }

    private String readString() {
        try {
            return Files.readString(Paths.get(getClass().getResource("/client-request-body.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}

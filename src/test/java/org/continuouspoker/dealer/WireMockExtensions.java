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
package org.continuouspoker.dealer;

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
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {
    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        final String json = readString();
        wireMockServer.stubFor(post(urlEqualTo("/")).withRequestBody(equalToJson(json))
                                                    .willReturn(
                                                            aResponse().withHeader("Content-Type", "application/json")
                                                                       .withBody("{ \"bet\": 5 }")));

        return Collections.singletonMap("quarkus.rest-client.\"org.continuouspoker.dealer.RemotePlayerClient\".url",
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

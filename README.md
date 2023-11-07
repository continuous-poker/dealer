# Continuous Poker Dealer

The poker dealer is a part of the continuous poker workshop and contains the base logic to orchestrate poker games.
If all players have successfully deployed their application and registered it with the game,
the dealer service will start playing tournaments over and over.
It requests bets from each player every round and determines the winner after a round is finished.

The counterparts are the poker player repositories where you are able to implement the logic that plays the game for you.
For further information, see the [repository overview](https://github.com/orgs/continuous-poker/repositories).

## Prerequisites

- Java >= 17

## Usage

1. Start the application using `./mvnw quarkus:dev`
2. Open `http://localhost:8080/admin.html` in your browser.
3. Create a new game. The default credentials are `admin:admin`.
4. Add new players to the game. Enter the full URL of the player endpoints, e.g. `http://localhost:8081`
5. Start your player application.
6. Start the game on the admin page. 
7. To analyze the game, open the dashboard at `http://localhost:8080/index.html`

## Contribution

If you want to participate in development, look up the open [issues](https://github.com/continuous-poker/dealer/issues).
				
## License

This program and the accompanying materials are made available under the terms
of the Apache License, Version 2.0 which is available at
https://www.apache.org/licenses/LICENSE-2.0.

SPDX-License-Identifier: Apache-2.0
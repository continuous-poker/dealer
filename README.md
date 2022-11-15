# Poker Project - The Poker Dealer

The poker dealer is a part of the poker project and contains the base logic for a poker game.
If all players have successfully deployed their application and registered it with the game,
the dealer service will start playing tournaments over and over.
It takes the action of each player(bot) every round and determines the winner after all necessary cards / bets are placed.

The counterparts are the poker players (Quarkus & Spring Boot)
where you are able to implement your player logic that plays for you.
For further information visit
[continuous-poker-player-quarkus](https://github.com/ds-jkreutzfeld/continuous-poker-player-quarkus) &
[continuous-poker-player-spring-boot](https://github.com/ds-jkreutzfeld/continuous-poker-player-spring-boot).

## How do you create & start a new game?

### 1. Fork the repository.
### 2. Install and run the application in your IDE.
### 3. Open `http://localhost:8080/q/swagger-ui/` in your browser.
### 4. Choose a name for your game and create it via the `POST /games`.
> **_Info:_** The `gameId` in the response body is important for configuration of the game.

### 5. Add new players to the game via the `POST /games/{gameId}/players`.
 1. Fill in the *gameId*, *playerUrl* and the *playerName* (teamName).
 2.  The playerUrl is the complete url and needs to match with port of the application.yml of the Poker Player! Example: http://localhost:8081 
> **_Info:_** If the port is missing, add the following code with the right formatting: 
> - quarkus: http: port: `matching port`

> **_Info:_** You can check all players by GET / games / { `gameId` } / players.

### 6. Start your player application.

### 7. Now you can start the game via the `PUT /games/{gameId}` and you will see how the game progresses in the console of your IDE. 
> **_Info:_** For a better presentation open `http://localhost:8080/log.html?gameId = "insert your gameId"`

## Help develop the Dealer

If you want to participate in development, look up the open [issues](https://github.com/ds-jkreutzfeld/continuous-poker-dealer/issues).
				

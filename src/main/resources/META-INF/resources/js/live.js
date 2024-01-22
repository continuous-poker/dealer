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
Vue.createApp({
    data() {
        return {
            games: [],
            gameId: -1,
            score: null,
            tournamentId: null,
            roundId: null,
            table: {players: [], communityCards: []},
            gameHistory: null,      //Object for whole history
            displayedHistory: null, //Object for currently displayed history
        }
    },


    created() {
        this.update();
        this.timer = setInterval(this.update, 1000);
    },

    beforeDestroy() {
        this.cancelAutoUpdate();
    },

    mounted() {
        // Fetch data from the /games endpoint using a GET request
        this.loadGames();
    },

    methods: {
        loadGames() {
            fetch('/games')
                .then(response => response.json())
                .then(data => {
                    this.games = data; // Update the games data in Vue
                })
                .catch(error => {
                    console.error('Error fetching data:', error);
                });
        },

        getImage(card) {
            if (card.suit && card.rank) {
                return String.fromCodePoint(parseInt("1F0"+this.suitToLetter(card.suit)+this.rankToLetter(card.rank), 16));
            }
        },

        selectTournament(tournament) {
            this.tournamentId = tournament;
            this.roundId = null;
        },

        selectRound(round) {
            this.roundId = round;
            this.displayedHistory = this.gameHistory[this.tournamentId][round];
            this.update();
        },


        suitToLetter(suit) {
           var pairs = {
               "SPADES": "A",
               "HEARTS": "B",
               "DIAMONDS": "C",
               "CLUBS": "D"
           }
           return pairs[suit];
        },

        rankToLetter(suit) {
           var pairs = {
               "A": "1",
               "2": "2",
               "3": "3",
               "4": "4",
               "5": "5",
               "6": "6",
               "7": "7",
               "8": "8",
               "9": "9",
               "10": "A",
               "J": "B",
               "Q": "D",
               "K": "E"
           }
           return pairs[suit];
        },

        getCardValue(value) {
            switch (value) {
                case "J":
                    return "jack";
                case "Q":
                    return "queen";
                case "K":
                    return "king";
                case "A":
                    return "1";
                default:
                    return value;
            }
        },

        update() {
            // Simple GET request using fetch
            if (this.gameId !== -1) {
                axios
                    .get("/games/" + this.gameId + "/latestIds")
                    .then(response => {
                        const logs = response.data;
                        this.tournamentId = logs.tournamentId;
                        this.roundId = logs.roundId;
                    });

                axios
                    .get("/games/" + this.gameId + "/score")
                    .then(response => {
                        let sortable = [];
                        for (var team in response.data) {
                            sortable.push([team, response.data[team]]);
                        }
                        sortable.sort(function (a, b) {
                            return b[1] - a[1];
                        });
                        this.score = sortable;
                    });


                if (this.tournamentId != null) {
                    this.updateTable(this.gameId, this.tournamentId, this.roundId);
                }
                // axios
                //     .get("/games/" + this.gameId + "/history")
                //     .then(response => {
                //         this.gameHistory = response.data;
                //         if (!this.gameHistory[this.tournamentId]) {
                //             this.tournamentId = null;
                //             this.roundId = null;
                //         }
                //     });
                //
                // this.displayedHistory = this.gameHistory[this.tournamentId][this.roundId];
            }
        },

        updateTable(gameId, tournamentId, roundId) {
            axios
                .get("/games/" + gameId + "/tournament/" + tournamentId + (roundId == null ? "" : "/round/" + roundId))
                .then(response => {
                    this.table = response.data;
                });
        },

        cancelAutoUpdate() {
            clearInterval(this.timer);
        }
    }
}).mount('#app')
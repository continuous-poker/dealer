Vue.createApp({
    data() {
        return {
            logs: [],
            gamestate: {state: ""},
            score: null,
            tournamentHistoryOpenState: {},
            gameId: null,
            tournamentId: null,
            table: {players: [], communityCards: []},

            gameLog: true,          //show currentLog
            legacyLog: false,       //show historyLog

            gameHistory: null,      //Object for whole history
            displayedHistory: null, //Object for currently displayed history

            displayLogTable: null,  //show a full table
            displayLogRound: null,  //show a round

            displayPlayers: true,
            displayScoreboard: false,
        }
    },


    created() {
        this.update();
        this.timer = setInterval(this.update, 1000);
    },

    beforeDestroy() {
        this.cancelAutoUpdate();
    },

    methods: {
        historyIsOpen(index) {
            if (this.tournamentHistoryOpenState[index] != null) {
                return this.tournamentHistoryOpenState[index];
            }
            return false;
        },
        setHistoryOpen(tournamentIndex, opened) {
            this.tournamentHistoryOpenState[tournamentIndex] = opened;
        },
        displayHandler(action, tournamentId, gameRound) {
            switch (action) {
                case "x":
                    this.gameLog = true;
                    this.legacyLog = false;
                    break;
                case "round":
                    this.displayedHistory = this.gameHistory[tournamentId][gameRound];
                    this.legacyLog = true;
                    this.gameLog = false;

                    this.displayLogRound = true;
                    this.displayLogTable = false;
                    this.updateTable(this.gameId, tournamentId, gameRound);
                    break;
                case "tournament":
                    this.displayedHistory = this.gameHistory[tournamentId];
                    this.legacyLog = true;
                    this.gameLog = false;

                    this.displayLogTable = true;
                    this.displayLogRound = false;
                    this.table = {players: [], communityCards: []};
                    break;

                case 'scoreboard':
                    this.displayScoreboard = true;
                    this.displayPlayers = false;
                    break;
                case 'players':
                    this.displayPlayers = true;
                    this.displayScoreboard = false;
                    break;
            }
        },

        getImageUrl(card) {
            if (card.suit && card.rank) {
                return "Playcards/png/2x/" + (card.suit).toLowerCase().slice(0, -1) + "_" + this.getCardValue(card.rank) + ".png";
            }
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
            this.gameId = new URL(location.href).searchParams.get('gameId')
            if (this.gameId != null) {
                axios
                    .get("/games/" + this.gameId + "/log?order=desc&limit=50")
                    .then(response => {
                        this.logs = response.data;
                        if (this.logs.length > 0) {
                            this.tournamentId = this.logs[this.logs.length - 1].tournamentId;
                        }
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
                axios
                    .get("/games/" + this.gameId)
                    .then(response => {
                        this.gamestate = response.data;
                    });

                if (this.tournamentId != null && this.gameLog) {
                    this.updateTable(this.gameId, this.tournamentId);
                }
                axios
                    .get("/games/" + this.gameId + "/history")
                    .then(response => {
                        this.gameHistory = response.data;
                    });
            }
        },

        updateTable(gameId, tournamentId, gameRound) {
            axios
                .get("/games/" + gameId + "/tournament/" + tournamentId + (gameRound == null ? "" : "/round/" + gameRound))
                .then(response => {
                    this.table = response.data;
                });
        },

        cancelAutoUpdate() {
            clearInterval(this.timer);
        }
    }
}).mount('#app')
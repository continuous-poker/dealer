Vue.createApp({
    data() {
        return {
            games: [],           // Store the retrieved game data
            showDialog: false,   // Flag to show/hide the create game dialog
            newGameName: '',     // Store the new game name
            selectedGame: null,  // Store the selected game details
        };
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
                    if (this.selectedGame) {
                        this.selectedGame = this.games.find(game => game.gameId === this.selectedGame.gameId);
                        this.fetchGameStatus(this.selectedGame.gameId);
                    }
                })
                .catch(error => {
                    console.error('Error fetching data:', error);
                });
        },
        showCreateGameDialog() {
            this.showDialog = true;
        },
        cancelCreateGame() {
            this.showDialog = false;
            this.newGameName = '';
        },
        createGame() {
            // Prepare the form data
            const formData = new URLSearchParams();
            formData.append('name', this.newGameName);

            // Send a POST request to /games/manage with form data
            fetch('/games/manage', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData,
            })
            .then(response => {
                if (response.ok) {
                    this.loadGames(); // Reload the game list on success
                    this.showDialog = false;
                    this.newGameName = '';
                } else {
                    console.error('Error creating game:', response.statusText);
                }
            })
            .catch(error => {
                console.error('Error creating game:', error);
            });
        },
        selectGame(game) {
            this.selectedGame = game;
            this.fetchGameStatus(this.selectedGame.gameId);
        },
        fetchGameStatus(gameId) {
            fetch(`/games/${gameId}`, {
                method: 'GET'
            })
            .then(response => response.json())
            .then(response => {
                if (this.selectedGame.gameId === gameId) {
                    this.selectedGame.status = response.state;
                }
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });
        },
        triggerStatus(gameId) {
            fetch(`/games/manage/${gameId}`, {
                method: 'PUT'
            })
            .then(response => {
                this.fetchGameStatus(gameId);
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });

        },
        addNewTeam() {
                if (this.selectedGame) {
                    const gameId = this.selectedGame.gameId;

                    // Prepare the query parameters for team name and URL
                    const queryParams = new URLSearchParams();
                    queryParams.set('teamName', this.newTeamName);
                    queryParams.set('playerUrl', this.newTeamURL);

                    // Send a POST request to /games/manage/{gameId}/players with query parameters
                    fetch(`/games/manage/${gameId}/players?${queryParams.toString()}`, {
                        method: 'POST',
                    })
                    .then(response => {
                        if (response.ok) {
                            // Reload the selected game to update the team list
                            this.loadGames();
                            this.newTeamName = '';
                            this.newTeamURL = '';
                        } else {
                            console.error('Error adding team:', response.statusText);
                        }
                    })
                    .catch(error => {
                        console.error('Error adding team:', error);
                    });
                }
            },
            removeTeam(teamName) {
                if (this.selectedGame) {
                    const gameId = this.selectedGame.gameId;

                    // Prepare the query parameters for team name and URL
                    const queryParams = new URLSearchParams();
                    queryParams.set('teamName', teamName);

                    // Send a POST request to /games/manage/{gameId}/players with query parameters
                    fetch(`/games/manage/${gameId}/players?${queryParams.toString()}`, {
                        method: 'DELETE',
                    })
                    .then(response => {
                        if (response.ok) {
                            // Reload the selected game to update the team list
                            this.loadGames();
                        } else {
                            console.error('Error removing team:', response.statusText);
                        }
                    })
                    .catch(error => {
                        console.error('Error removing team:', error);
                    });
                }
            },
            removeGame(gameId) {
                fetch(`/games/manage/${gameId}`, {
                    method: 'DELETE',
                })
                .then(response => {
                    if (response.ok) {
                        this.selectedGame = undefined;
                        this.loadGames();
                    } else {
                        console.error('Error removing game:', response.statusText);
                    }
                })
                .catch(error => {
                    console.error('Error removing game:', error);
                });
            }
    },
}).mount('#app');
const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {games: []};
        this.handleNewGame = this.handleNewGame.bind(this);

    }

    componentDidMount() {
        this.updateGameList();
    }

    updateGameList() {
        client({method: 'GET', path: '/games'}).done(response => {
            this.setState({games: response.entity});
        });
    }

    handleNewGame() {

        client({method: 'POST', path: '/games', query: { name: "nameOfGame" }}).done(response => {
            this.setState({games: response.entity});
        });
    }

    render() {
        return (
            <div>
                <GameList games={this.state.games}/>
                <input type="text" placeholder="Game name" />
                <button onClick={this.handleNewGame}>Create new game</button>
            </div>
        )
    }
}

class GameList extends React.Component{

    constructor(props) {
        super(props);
    }



    render() {
        const games = this.props.games.map(game =>
            <Game key={game.gameId} game={game}/>
        );
        return (
                <table>
                    <tbody>
                    <tr>
                        <th>Name</th>
                    </tr>
                    {games}
                    </tbody>
                </table>
        )
    }
}

class Game extends React.Component{
    render() {
        return (
            <tr>
                <td>{this.props.game.name}</td>
            </tr>
        )
    }
}

ReactDOM.render(
    <App />,
    document.getElementById('react')
)
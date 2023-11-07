package org.continuouspoker.dealer.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.game.Game;

@ApplicationScoped
public class GameDAO {

    @Transactional
    public void storeGames(final Set<Game> games) {
        games.stream().map(GameDAO::toGameBE).forEach(g -> g.persist());
    }

    @Transactional
    public GameBE createGame(final Game game) {
        final GameBE gameBE = toGameBE(game);
        gameBE.persist();
        return gameBE;
    }

    private static GameBE toGameBE(final Game g) {
        final Optional<GameBE> game = GameBE.findByIdOptional(g.getGameId());
        if (game.isPresent()) {
            final GameBE gameBE = game.get();
            gameBE.setName(g.getName());
            gameBE.setTeams(g.getTeams().stream().map(GameDAO::toTeamBE).toList());
            return gameBE;
        } else {
            return new GameBE(g.getName(), g.getTeams().stream().map(GameDAO::toTeamBE).toList());
        }
    }

    private static TeamBE toTeamBE(Team t) {
        final Optional<TeamBE> team = TeamBE.findByIdOptional(t.getTeamId());
        if (team.isPresent()) {
            final TeamBE teamBE = team.get();
            teamBE.setScore(t.getScore());
            teamBE.setProviderUrl(t.getProvider().getUrl());
            return teamBE;
        } else {
            return new TeamBE(t.getName(), t.getScore(), t.getProvider().getUrl());
        }
    }

    public List<GameBE> loadGames() {
        return GameBE.listAll(Sort.by("name"));
    }

    @Transactional
    public TeamBE createTeam(final String teamName, final String playerUrl) {
        final TeamBE teamBE = new TeamBE(teamName, 0, playerUrl);
        teamBE.persist();
        return teamBE;
    }
}

package org.continuouspoker.dealer.persistence;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.game.Game;

@ApplicationScoped
public class GameDAO {

    private static final int SCORE_LIMIT = 100;

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

    private static GameBE toGameBE(final Game source) {
        final Optional<GameBE> game = GameBE.findByIdOptional(source.getGameId());
        if (game.isPresent()) {
            final GameBE gameBE = game.get();
            gameBE.setName(source.getName());
            gameBE.setTeams(source.getTeams().stream().map(GameDAO::toTeamBE).toList());
            return gameBE;
        } else {
            return new GameBE(source.getName(), source.getTeams().stream().map(GameDAO::toTeamBE).toList());
        }
    }

    private static TeamBE toTeamBE(final Team source) {
        final Optional<TeamBE> team = TeamBE.findByIdOptional(source.getTeamId());
        if (team.isPresent()) {
            final TeamBE teamBE = team.get();
            teamBE.setScore(source.getScore());
            teamBE.setProviderUrl(source.getProvider().getUrl());
            return teamBE;
        } else {
            return new TeamBE(source.getName(), source.getScore(), source.getProvider().getUrl());
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

    @Transactional
    public void storeScores(final Game game) {
        final Set<TeamScoreRecordBE> teamScores = new HashSet<>(
                game.getTeams().stream().map(this::storeScore).toList());

        new ScoreRecordBE(Instant.now(), game.getGameId(), teamScores).persist();
    }

    private TeamScoreRecordBE storeScore(final Team team) {
        final TeamScoreRecordBE score = new TeamScoreRecordBE(toTeamBE(team), team.getScore());
        score.persist();
        return score;
    }

    public List<ScoreRecordBE> loadScores(final long gameId) {
        final PanacheQuery<PanacheEntityBase> query = ScoreRecordBE.find("gameId",
                Sort.by("creationTimestamp", Sort.Direction.Descending), gameId);
        query.range(0, SCORE_LIMIT);
        return query.list();
    }
}

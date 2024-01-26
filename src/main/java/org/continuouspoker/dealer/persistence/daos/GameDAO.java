package org.continuouspoker.dealer.persistence.daos;

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
import org.continuouspoker.dealer.persistence.entities.GameBE;
import org.continuouspoker.dealer.persistence.entities.ScoreRecordBE;
import org.continuouspoker.dealer.persistence.entities.TeamBE;
import org.continuouspoker.dealer.persistence.entities.TeamScoreRecordBE;
import org.continuouspoker.dealer.persistence.mappers.GameMapper;
import org.continuouspoker.dealer.persistence.mappers.TeamMapper;
import org.mapstruct.factory.Mappers;

@ApplicationScoped
public class GameDAO {
    private static final int SCORE_LIMIT = 100;
    TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);
    GameMapper gameMapper = Mappers.getMapper(GameMapper.class);

    // Games
    @Transactional
    public void storeGames(final Set<Game> games) {
        games.stream().map(game -> gameMapper.toEntity(game)).forEach(g -> g.persist());
    }

    @Transactional
    public void createGame(final Game game) {
        final GameBE gameBE = gameMapper.toEntity(game);
        gameBE.persist();
    }

    public Optional<List<Game>> loadGames() {
        List<GameBE> games = GameBE.listAll(Sort.by("name"));
        return Optional.of(games.stream().map(g -> gameMapper.toDto(g)).toList());
    }

    @Transactional
    public Team createTeam(final String teamName, final String playerUrl) {
        final TeamBE teamBE = new TeamBE(teamName, 0, playerUrl);
        teamBE.persist();
        return teamMapper.toDto(teamBE);
    }

    // Scores
    @Transactional
    public void storeScores(final Game game) {
        final Set<TeamScoreRecordBE> teamScores = new HashSet<>(
                game.getTeams().stream()
                    .map(this::storeScore)
                    .filter(Optional::isPresent)
                    .map(Optional::get).toList());

        new ScoreRecordBE(Instant.now(), game.getGameId(), teamScores).persist();
    }

    private Optional<TeamScoreRecordBE> storeScore(final Team team) {
        final TeamScoreRecordBE score = new TeamScoreRecordBE(teamMapper.toEntity(team), team.getScore());
        score.persist();
        return Optional.of(score);
    }

    public Optional<List<ScoreRecordBE>> loadScores(final long gameId) {
        final PanacheQuery<PanacheEntityBase> query = ScoreRecordBE.find("gameId",
                Sort.by("creationTimestamp", Sort.Direction.Descending), gameId);
        query.range(0, SCORE_LIMIT);
        return Optional.of(query.list());
    }
}

/*
 * Copyright © 2020-2024 Jan Kreutzfeld
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

package org.continuouspoker.dealer.persistence.daos;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.entities.GameBE;
import org.continuouspoker.dealer.persistence.entities.ScoreRecordBE;
import org.continuouspoker.dealer.persistence.entities.TeamBE;
import org.continuouspoker.dealer.persistence.entities.TeamScoreRecordBE;
import org.continuouspoker.dealer.persistence.mappers.GameMapper;
import org.continuouspoker.dealer.persistence.mappers.TeamMapper;

@ApplicationScoped
public class GameDAO {
    private static final int SCORE_LIMIT = 100;
    @Inject
    TeamMapper teamMapper;
    @Inject
    GameMapper gameMapper;

    @Transactional
    public void storeGames(final Set<Game> games) {
        games.stream().map(gameMapper::toEntity).forEach(g -> g.persist());
    }

    @Transactional
    public void createGame(final Game game) {
        final GameBE gameBE = gameMapper.toEntity(game);
        gameBE.persist();
    }

    public List<Game> loadGames() {
        List<GameBE> games = GameBE.listAll(Sort.by("name"));
        return games.stream().map(gameMapper::toDto).toList();
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
                    .map(this::storeScore).toList());

        new ScoreRecordBE(Instant.now(), game.getGameId(), teamScores).persist();
    }

    private TeamScoreRecordBE storeScore(final Team team) {
        final TeamScoreRecordBE score = new TeamScoreRecordBE(teamMapper.toEntity(team), team.getScore());
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

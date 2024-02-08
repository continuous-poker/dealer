/*
 * Copyright © 2020-2024 doubleSlash Net-Business GmbH
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

package org.continuouspoker.dealer.persistence.mappers;

import static org.mapstruct.CollectionMappingStrategy.TARGET_IMMUTABLE;

import java.util.List;

import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.game.Game;
import org.continuouspoker.dealer.persistence.entities.GameBE;
import org.continuouspoker.dealer.persistence.entities.TeamBE;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        collectionMappingStrategy = TARGET_IMMUTABLE)
public interface GameMapper {
    GameBE toEntity(Game game);
    Game toDto(GameBE gameBE);
    List<TeamBE> toTeamBE(List<Team> teams);
    default TeamBE teamToTeamBE(Team team) {
        return Mappers.getMapper(TeamMapper.class).toEntity(team);
    }
    default Team teamBEtoTeam(TeamBE teamBE) {
        return Mappers.getMapper(TeamMapper.class).toDto(teamBE);
    }
}

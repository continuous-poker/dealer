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

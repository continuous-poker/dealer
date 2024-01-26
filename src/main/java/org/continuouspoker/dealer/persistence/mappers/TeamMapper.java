package org.continuouspoker.dealer.persistence.mappers;

import org.continuouspoker.dealer.RemotePlayer;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.persistence.entities.TeamBE;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {
    default Team toDto(TeamBE teamBE) {
        Team team = new Team(teamBE.id, teamBE.getName(),
                new RemotePlayer(teamBE.getProviderUrl()));
        team.addToScore(teamBE.getScore());
        return team;
    }

    default TeamBE toEntity(Team team) {
        return new TeamBE(team.getName(), team.getScore(), team.getProvider().getUrl());
    }
}

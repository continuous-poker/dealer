package org.continuouspoker.dealer.persistence.mappers;

import static org.junit.jupiter.api.Assertions.*;

import org.continuouspoker.dealer.RemotePlayer;
import org.continuouspoker.dealer.Team;
import org.continuouspoker.dealer.persistence.entities.TeamBE;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class TeamMapperTest {
    private TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);
    @Test
    public void testEntityToModel() {
        TeamBE teamBE = new TeamBE();
        teamBE.setName("X");
        teamBE.setScore(1L);
        teamBE.setProviderUrl("http://remotePlayer");

        Team team = teamMapper.toDto(teamBE);

        assertEquals(team.getName(), teamBE.getName());
        assertEquals(team.getScore(), teamBE.getScore());
        assertEquals(team.getProvider().getUrl(), teamBE.getProviderUrl());
    }

    @Test
    public void testModelToEntity() {
        Team team = new Team(1L, "model", new RemotePlayer("http://remotePlayer"));

        TeamBE teamBE = teamMapper.toEntity(team);

        assertEquals(teamBE.getName(), team.getName());
        assertEquals(teamBE.getScore(), team.getScore());
        assertEquals( teamBE.getProviderUrl(), team.getProvider().getUrl());
    }

}
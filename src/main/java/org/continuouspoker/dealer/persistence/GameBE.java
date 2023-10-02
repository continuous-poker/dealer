package org.continuouspoker.dealer.persistence;

import java.util.List;

public record GameBE(long gameId, String name, List<TeamBE> teams) {

}

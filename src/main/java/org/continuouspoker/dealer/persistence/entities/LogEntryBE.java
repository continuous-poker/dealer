package org.continuouspoker.dealer.persistence.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "logs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogEntryBE extends PanacheEntity {

    private long gameId;

    private long tournamentId;

    private long roundId;

    private String message;

}

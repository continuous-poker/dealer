package org.continuouspoker.dealer.persistence.entities;

import java.time.Instant;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "scores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScoreRecordBE extends PanacheEntity {
    private Instant creationTimestamp;
    private long gameId;

    @OneToMany
    private Set<TeamScoreRecordBE> teamScores;
}

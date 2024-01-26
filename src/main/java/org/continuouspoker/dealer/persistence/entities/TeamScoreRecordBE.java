package org.continuouspoker.dealer.persistence.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "teamscores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TeamScoreRecordBE extends PanacheEntity {

    @ManyToOne(cascade = CascadeType.PERSIST)
    private TeamBE team;

    private long score;
}

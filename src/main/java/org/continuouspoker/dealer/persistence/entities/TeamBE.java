package org.continuouspoker.dealer.persistence.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "teams")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TeamBE extends PanacheEntity {
    private String name;
    private long score;
    private String providerUrl;
}

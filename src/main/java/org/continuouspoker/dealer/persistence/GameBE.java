package org.continuouspoker.dealer.persistence;

import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "games")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameBE extends PanacheEntity {

    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    private List<TeamBE> teams;

}

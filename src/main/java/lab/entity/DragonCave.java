package lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "dragon_cave")
public class DragonCave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "depth", nullable = false)
    private double depth;

    @NotNull(message = "Укажите число сокровищ")
    @Positive(message = "Число сокровищ должно быть больше 0")
    @Column(name = "number_of_treasures", nullable = false)
    private Long numberOfTreasures;

    public Integer getId() {
        return id;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public Long getNumberOfTreasures() {
        return numberOfTreasures;
    }

    public void setNumberOfTreasures(Long numberOfTreasures) {
        this.numberOfTreasures = numberOfTreasures;
    }
}

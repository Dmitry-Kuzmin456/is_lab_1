package lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "dragon")
public class Dragon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 255, message = "Имя не длиннее 255 символов")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull(message = "Укажите координаты")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @NotNull(message = "Дата создания не может быть пустой")
    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @NotNull(message = "Укажите пещеру")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cave_id", nullable = false)
    private DragonCave cave;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "killer_id")
    private Person killer;

    @Positive(message = "Возраст должен быть больше 0")
    @Column(name = "age", nullable = false)
    private long age;

    @Positive(message = "Размах крыльев должен быть больше 0")
    @Column(name = "wingspan", nullable = false)
    private float wingspan;

    @NotNull(message = "Вес не может быть пустым")
    @Positive(message = "Вес должен быть больше 0")
    @Column(name = "weight", nullable = false)
    private Float weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "dragon_character", length = 32)
    private DragonCharacter character;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "head_id")
    private DragonHead head;

    @PrePersist
    public void fillCreationDate() {
        if (creationDate == null) {
            creationDate = LocalDate.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public DragonCave getCave() {
        return cave;
    }

    public void setCave(DragonCave cave) {
        this.cave = cave;
    }

    public Person getKiller() {
        return killer;
    }

    public void setKiller(Person killer) {
        this.killer = killer;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public float getWingspan() {
        return wingspan;
    }

    public void setWingspan(float wingspan) {
        this.wingspan = wingspan;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public DragonCharacter getCharacter() {
        return character;
    }

    public void setCharacter(DragonCharacter character) {
        this.character = character;
    }

    public DragonHead getHead() {
        return head;
    }

    public void setHead(DragonHead head) {
        this.head = head;
    }
}

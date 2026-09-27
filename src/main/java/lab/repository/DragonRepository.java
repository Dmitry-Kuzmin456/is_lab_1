package lab.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lab.entity.Coordinates;
import lab.entity.Dragon;
import lab.entity.DragonCave;
import lab.entity.DragonHead;
import lab.entity.Location;
import lab.entity.Person;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DragonRepository {

    public static final List<String> FIELDS = List.of(
            "name",
            "character",
            "killerName",
            "eyeColor",
            "hairColor",
            "nationality",
            "locationName"
    );

    private static final String FROM = " from dragon d "
            + "left join person k on k.id = d.killer_id "
            + "left join location l on l.id = k.location_id ";

    @PersistenceContext(unitName = "lab1")
    private EntityManager em;

    public DragonPage search(int page, int size, String sort, String direction, String field, String query) {
        String filter = expression(field);
        if (query != null && filter == null) {
            throw new IllegalArgumentException("Фильтровать можно по имени, характеру, убийце, цвету глаз, цвету волос, стране и названию места");
        }
        String sortExpression = expression(sort);
        if (sort != null && sortExpression == null) {
            throw new IllegalArgumentException("Сортировать можно только по строковой колонке");
        }
        String order = sortExpression == null
                ? "d.id asc"
                : sortExpression + " " + ("desc".equalsIgnoreCase(direction) ? "desc" : "asc") + ", d.id asc";
        String where = "";
        if (query != null) {
            where = " where strpos(" + filter + ", :q) > 0 ";
        }
        var countQuery = em.createNativeQuery("select count(d.id) " + FROM + where);
        if (query != null) {
            countQuery.setParameter("q", query);
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();
        var idQuery = em.createNativeQuery("select d.id " + FROM + where + " order by " + order + " limit :lim offset :off");
        if (query != null) {
            idQuery.setParameter("q", query);
        }
        idQuery.setParameter("lim", size);
        idQuery.setParameter("off", page * (long) size);
        List<Integer> ids = integers(idQuery.getResultList());
        if (ids.isEmpty()) {
            return new DragonPage(new ArrayList<>(), total);
        }
        List<Dragon> loaded = em.createQuery("select d from Dragon d where d.id in :ids", Dragon.class)
                .setParameter("ids", ids)
                .getResultList();
        Map<Integer, Dragon> byId = new LinkedHashMap<>();
        for (Dragon dragon : loaded) {
            byId.put(dragon.getId(), dragon);
        }
        List<Dragon> ordered = new ArrayList<>();
        for (Integer id : ids) {
            Dragon dragon = byId.get(id);
            if (dragon != null) {
                ordered.add(dragon);
            }
        }
        return new DragonPage(ordered, total);
    }

    public Dragon find(int id) {
        return em.find(Dragon.class, id);
    }

    public Coordinates coordinates(int id) {
        return em.find(Coordinates.class, id);
    }

    public DragonCave cave(int id) {
        return em.find(DragonCave.class, id);
    }

    public Person person(int id) {
        return em.find(Person.class, id);
    }

    public DragonHead head(int id) {
        return em.find(DragonHead.class, id);
    }

    public Location location(int id) {
        return em.find(Location.class, id);
    }

    public List<Coordinates> allCoordinates() {
        return em.createQuery("select c from Coordinates c order by c.id", Coordinates.class).getResultList();
    }

    public List<DragonCave> allCaves() {
        return em.createQuery("select c from DragonCave c order by c.id", DragonCave.class).getResultList();
    }

    public List<Person> allPersons() {
        return em.createQuery("select p from Person p order by p.id", Person.class).getResultList();
    }

    public List<DragonHead> allHeads() {
        return em.createQuery("select h from DragonHead h order by h.id", DragonHead.class).getResultList();
    }

    public List<Location> allLocations() {
        return em.createQuery("select l from Location l order by l.id", Location.class).getResultList();
    }

    public void persist(Object entity) {
        em.persist(entity);
    }

    public void flush() {
        em.flush();
    }

    public void deleteDragon(Dragon dragon) {
        Coordinates coordinates = dragon.getCoordinates();
        DragonCave cave = dragon.getCave();
        Person killer = dragon.getKiller();
        DragonHead head = dragon.getHead();
        em.remove(dragon);
        em.flush();
        removeCoordinates(coordinates);
        removeCave(cave);
        removeHead(head);
        removePerson(killer);
        em.flush();
    }

    private void removeCoordinates(Coordinates coordinates) {
        if (coordinates == null || coordinates.getId() == null) {
            return;
        }
        long used = em.createQuery("select count(d.id) from Dragon d where d.coordinates.id = :id", Long.class)
                .setParameter("id", coordinates.getId())
                .getSingleResult();
        if (used == 0) {
            em.remove(coordinates);
        }
    }

    private void removeCave(DragonCave cave) {
        if (cave == null || cave.getId() == null) {
            return;
        }
        long used = em.createQuery("select count(d.id) from Dragon d where d.cave.id = :id", Long.class)
                .setParameter("id", cave.getId())
                .getSingleResult();
        if (used > 0) {
            return;
        }
        em.createQuery("update KillerTeam t set t.cave = null where t.cave.id = :id")
                .setParameter("id", cave.getId())
                .executeUpdate();
        em.remove(em.contains(cave) ? cave : em.merge(cave));
    }

    private void removeHead(DragonHead head) {
        if (head == null || head.getId() == null) {
            return;
        }
        long used = em.createQuery("select count(d.id) from Dragon d where d.head.id = :id", Long.class)
                .setParameter("id", head.getId())
                .getSingleResult();
        if (used == 0) {
            em.remove(head);
        }
    }

    private void removePerson(Person person) {
        if (person == null || person.getId() == null) {
            return;
        }
        long used = em.createQuery("select count(d.id) from Dragon d where d.killer.id = :id", Long.class)
                .setParameter("id", person.getId())
                .getSingleResult();
        if (used > 0) {
            return;
        }
        em.createNativeQuery("delete from killer_team_member where person_id = :id")
                .setParameter("id", person.getId())
                .executeUpdate();
        Location location = person.getLocation();
        em.remove(em.contains(person) ? person : em.merge(person));
        em.flush();
        removeLocation(location);
    }

    private void removeLocation(Location location) {
        if (location == null || location.getId() == null) {
            return;
        }
        long used = em.createQuery("select count(p.id) from Person p where p.location.id = :id", Long.class)
                .setParameter("id", location.getId())
                .getSingleResult();
        if (used == 0) {
            em.remove(em.contains(location) ? location : em.merge(location));
        }
    }

    private String expression(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return switch (key) {
            case "name" -> "d.name";
            case "character" -> "case d.dragon_character "
                    + "when 'WISE' then 'Мудрый' "
                    + "when 'GOOD' then 'Добрый' "
                    + "when 'CHAOTIC' then 'Хаотичный' "
                    + "when 'FICKLE' then 'Непостоянный' "
                    + "else '' end";
            case "killerName" -> "coalesce(k.name, '')";
            case "eyeColor" -> colorText("k.eye_color");
            case "hairColor" -> colorText("k.hair_color");
            case "nationality" -> "case k.nationality "
                    + "when 'RUSSIA' then 'Россия' "
                    + "when 'USA' then 'США' "
                    + "when 'GERMANY' then 'Германия' "
                    + "when 'SPAIN' then 'Испания' "
                    + "when 'INDIA' then 'Индия' "
                    + "else '' end";
            case "locationName" -> "coalesce(l.name, '')";
            default -> null;
        };
    }

    private static String colorText(String column) {
        return "case " + column + " "
                + "when 'GREEN' then 'Зелёный' "
                + "when 'BLACK' then 'Чёрный' "
                + "when 'BLUE' then 'Синий' "
                + "when 'YELLOW' then 'Жёлтый' "
                + "when 'ORANGE' then 'Оранжевый' "
                + "else '' end";
    }

    private static List<Integer> integers(List<?> rows) {
        List<Integer> ids = new ArrayList<>();
        for (Object row : rows) {
            if (row instanceof Number number) {
                ids.add(number.intValue());
            } else if (row instanceof Object[] cells && cells.length > 0 && cells[0] instanceof Number number) {
                ids.add(number.intValue());
            }
        }
        return ids;
    }
}

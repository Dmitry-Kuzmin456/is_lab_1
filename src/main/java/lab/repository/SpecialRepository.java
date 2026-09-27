package lab.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lab.entity.KillerTeam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SpecialRepository {

    @PersistenceContext(unitName = "lab1")
    private EntityManager em;

    public Integer dragonWithMaxCreationDate() {
        Object value = em.createNativeQuery("select dragon_with_max_creation_date()").getSingleResult();
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }

    public List<Integer> dragonsByName(String part) {
        return integers(em.createNativeQuery("select * from dragons_by_name_part(:part)")
                .setParameter("part", part)
                .getResultList());
    }

    public List<Integer> dragonsByCharacter(String character) {
        return integers(em.createNativeQuery("select * from dragons_by_character_less_than(:ch)")
                .setParameter("ch", character)
                .getResultList());
    }

    public int createTeam(String name, String memberIds) {
        Object value = em.createNativeQuery("select create_killer_team(:name, :members)")
                .setParameter("name", name)
                .setParameter("members", memberIds == null ? "" : memberIds)
                .getSingleResult();
        return ((Number) value).intValue();
    }

    public int sendTeam(int teamId, int caveId) {
        Object value = em.createNativeQuery("select send_killer_team_to_cave(:team, :cave)")
                .setParameter("team", teamId)
                .setParameter("cave", caveId)
                .getSingleResult();
        return ((Number) value).intValue();
    }

    public KillerTeam reloadTeam(int id) {
        em.flush();
        em.clear();
        return em.find(KillerTeam.class, id);
    }

    public List<KillerTeam> teams() {
        List<KillerTeam> teams = em.createQuery("select t from KillerTeam t order by t.id", KillerTeam.class).getResultList();
        Map<Integer, KillerTeam> unique = new LinkedHashMap<>();
        for (KillerTeam team : teams) {
            unique.put(team.getId(), team);
        }
        return new ArrayList<>(unique.values());
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

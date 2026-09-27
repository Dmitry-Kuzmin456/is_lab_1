package lab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lab.dto.DragonResponse;
import lab.dto.SendTeamRequest;
import lab.dto.TeamRequest;
import lab.dto.TeamResponse;
import lab.entity.Dragon;
import lab.entity.DragonCharacter;
import lab.entity.KillerTeam;
import lab.entity.Person;
import lab.mapper.DragonMapper;
import lab.repository.DragonRepository;
import lab.repository.SpecialRepository;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SpecialService {

    @Inject
    private SpecialRepository specials;

    @Inject
    private DragonRepository dragons;

    @Transactional
    public DragonResponse maxCreationDate() {
        try {
            Integer id = specials.dragonWithMaxCreationDate();
            if (id == null) {
                throw new MissingException("Драконов нет");
            }
            Dragon dragon = dragons.find(id);
            if (dragon == null) {
                throw new MissingException("Дракон не найден");
            }
            return DragonMapper.toResponse(dragon);
        } catch (PersistenceException exception) {
            throw dbError(exception);
        }
    }

    @Transactional
    public List<DragonResponse> byName(String part) {
        if (part == null || part.isBlank()) {
            throw new IllegalArgumentException("Укажите подстроку имени");
        }
        try {
            return load(specials.dragonsByName(part));
        } catch (PersistenceException exception) {
            throw dbError(exception);
        }
    }

    @Transactional
    public List<DragonResponse> byCharacter(String character) {
        if (character == null || character.isBlank()) {
            throw new IllegalArgumentException("Укажите характер");
        }
        try {
            DragonCharacter.valueOf(character.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Характер должен быть WISE, GOOD, CHAOTIC или FICKLE");
        }
        try {
            return load(specials.dragonsByCharacter(character.trim()));
        } catch (PersistenceException exception) {
            throw dbError(exception);
        }
    }

    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("Имя команды не может быть пустым");
        }
        StringBuilder members = new StringBuilder();
        if (request.memberIds != null) {
            for (String raw : request.memberIds) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                int id;
                try {
                    id = Integer.parseInt(raw.trim());
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Номер убийцы указан неверно");
                }
                if (dragons.person(id) == null) {
                    throw new IllegalArgumentException("Убийца с номером " + id + " не найден");
                }
                if (members.length() > 0) {
                    members.append(',');
                }
                members.append(id);
            }
        }
        try {
            int id = specials.createTeam(request.name.trim(), members.toString());
            return toResponse(requiredTeam(id));
        } catch (PersistenceException exception) {
            throw dbError(exception);
        }
    }

    @Transactional
    public TeamResponse send(SendTeamRequest request) {
        if (request == null || request.teamId == null || request.teamId.isBlank() || request.caveId == null || request.caveId.isBlank()) {
            throw new IllegalArgumentException("Выберите команду и пещеру");
        }
        int teamId;
        int caveId;
        try {
            teamId = Integer.parseInt(request.teamId.trim());
            caveId = Integer.parseInt(request.caveId.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Команда или пещера указаны неверно");
        }
        try {
            specials.sendTeam(teamId, caveId);
            return toResponse(requiredTeam(teamId));
        } catch (PersistenceException exception) {
            throw dbError(exception);
        }
    }

    @Transactional
    public List<TeamResponse> teams() {
        List<TeamResponse> result = new ArrayList<>();
        for (KillerTeam team : specials.teams()) {
            result.add(toResponse(team));
        }
        return result;
    }

    private KillerTeam requiredTeam(int id) {
        KillerTeam team = specials.reloadTeam(id);
        if (team == null) {
            throw new MissingException("Команда не найдена");
        }
        return team;
    }

    private List<DragonResponse> load(List<Integer> ids) {
        List<DragonResponse> result = new ArrayList<>();
        for (Integer id : ids) {
            Dragon dragon = dragons.find(id);
            if (dragon != null) {
                result.add(DragonMapper.toResponse(dragon));
            }
        }
        return result;
    }

    private TeamResponse toResponse(KillerTeam team) {
        TeamResponse response = new TeamResponse();
        response.id = team.getId();
        response.name = team.getName();
        if (team.getCave() != null) {
            response.caveId = team.getCave().getId();
            response.caveLabel = team.getCave().getId() + ": глубина " + team.getCave().getDepth();
        }
        if (team.getMembers() != null) {
            for (Person person : team.getMembers()) {
                response.members.add(person.getName());
            }
        }
        return response;
    }

    private IllegalArgumentException dbError(RuntimeException exception) {
        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage() == null ? "Операция базы данных не выполнена" : current.getMessage();
        message = message.replaceFirst("^ERROR:\\s*", "");
        int where = message.indexOf('\n');
        if (where >= 0) {
            message = message.substring(0, where).trim();
        }
        return new IllegalArgumentException(message);
    }
}

package lab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lab.dto.CaveOption;
import lab.dto.CoordinatesOption;
import lab.dto.DragonRequest;
import lab.dto.DragonResponse;
import lab.dto.HeadOption;
import lab.dto.KillerRequest;
import lab.dto.LocationOption;
import lab.dto.OptionsResponse;
import lab.dto.PageResponse;
import lab.dto.PersonOption;
import lab.entity.Color;
import lab.entity.Coordinates;
import lab.entity.Country;
import lab.entity.Dragon;
import lab.entity.DragonCave;
import lab.entity.DragonCharacter;
import lab.entity.DragonHead;
import lab.entity.Location;
import lab.entity.Person;
import lab.mapper.DragonMapper;
import lab.repository.DragonPage;
import lab.repository.DragonRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DragonService {

    private static final String EYE = "Цвет глаз убийцы должен быть GREEN, BLACK, BLUE, YELLOW или ORANGE";
    private static final String HAIR = "Цвет волос должен быть GREEN, BLACK, BLUE, YELLOW или ORANGE";
    private static final String COUNTRY = "Страна должна быть RUSSIA, USA, GERMANY, SPAIN или INDIA";
    private static final String CHARACTER = "Характер должен быть WISE, GOOD, CHAOTIC или FICKLE";

    @Inject
    private DragonRepository dragons;

    @Inject
    private Validator validator;

    @Transactional
    public PageResponse page(int page, int size, String sort, String direction, String field, String query) {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 5;
        }
        if (size > 50) {
            size = 50;
        }
        if (query != null && query.isBlank()) {
            query = null;
        }
        if (sort != null && sort.isBlank()) {
            sort = null;
        }
        if (field != null && field.isBlank()) {
            field = null;
        }
        if (query != null && (field == null || !DragonRepository.FIELDS.contains(field))) {
            throw new IllegalArgumentException("Выберите строковую колонку для фильтра");
        }
        DragonPage result = dragons.search(page, size, sort, direction, field, query);
        PageResponse response = new PageResponse();
        for (Dragon dragon : result.items) {
            response.items.add(DragonMapper.toResponse(dragon));
        }
        response.page = page;
        response.size = size;
        response.total = result.total;
        return response;
    }

    @Transactional
    public DragonResponse get(int id) {
        return DragonMapper.toResponse(required(id));
    }

    @Transactional
    public DragonResponse create(DragonRequest request) {
        Dragon dragon = new Dragon();
        fill(dragon, request);
        dragons.persist(dragon);
        dragons.flush();
        return DragonMapper.toResponse(dragon);
    }

    @Transactional
    public DragonResponse update(int id, DragonRequest request) {
        Dragon dragon = required(id);
        fill(dragon, request);
        dragons.flush();
        return DragonMapper.toResponse(dragon);
    }

    @Transactional
    public void delete(int id) {
        dragons.deleteDragon(required(id));
    }

    @Transactional
    public PersonOption createKiller(KillerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Нет данных убийцы");
        }
        DragonRequest mapped = new DragonRequest();
        mapped.killerName = request.name;
        mapped.eyeColor = request.eyeColor;
        mapped.hairColor = request.hairColor;
        mapped.nationality = request.nationality;
        mapped.birthday = request.birthday;
        mapped.locationId = request.locationId;
        mapped.locationX = request.locationX;
        mapped.locationY = request.locationY;
        mapped.locationZ = request.locationZ;
        mapped.locationName = request.locationName;
        List<String> errors = new ArrayList<>();
        Person person = killer(mapped, errors);
        if (person == null) {
            errors.add("Имя убийцы не может быть пустым");
        } else {
            errors.addAll(messages(person));
            if (person.getLocation() != null) {
                errors.addAll(messages(person.getLocation()));
            }
        }
        throwIfNeeded(errors);
        if (person.getLocation() != null && person.getLocation().getId() == null) {
            dragons.persist(person.getLocation());
        }
        dragons.persist(person);
        dragons.flush();
        return personOption(person);
    }

    @Transactional
    public OptionsResponse options() {
        OptionsResponse response = new OptionsResponse();
        for (Coordinates coordinates : dragons.allCoordinates()) {
            CoordinatesOption option = new CoordinatesOption();
            option.id = coordinates.getId();
            option.x = coordinates.getX();
            option.y = coordinates.getY();
            option.label = option.id + ": " + option.x + ", " + option.y;
            response.coordinates.add(option);
        }
        for (DragonCave cave : dragons.allCaves()) {
            CaveOption option = new CaveOption();
            option.id = cave.getId();
            option.depth = cave.getDepth();
            option.treasures = cave.getNumberOfTreasures();
            option.label = option.id + ": глубина " + option.depth + ", сокровища " + option.treasures;
            response.caves.add(option);
        }
        for (Person person : dragons.allPersons()) {
            response.persons.add(personOption(person));
        }
        for (DragonHead head : dragons.allHeads()) {
            HeadOption option = new HeadOption();
            option.id = head.getId();
            option.size = head.getSize();
            option.label = option.id + ": размер " + option.size;
            response.heads.add(option);
        }
        for (Location location : dragons.allLocations()) {
            LocationOption option = new LocationOption();
            option.id = location.getId();
            option.x = location.getX();
            option.y = location.getY();
            option.z = location.getZ();
            option.name = location.getName();
            option.label = option.id + ": " + option.x + ", " + option.y + ", " + option.z
                    + (option.name == null ? "" : ", " + option.name);
            response.locations.add(option);
        }
        response.characters = List.of("WISE", "GOOD", "CHAOTIC", "FICKLE");
        response.colors = List.of("GREEN", "BLACK", "BLUE", "YELLOW", "ORANGE");
        response.countries = List.of("RUSSIA", "USA", "GERMANY", "SPAIN", "INDIA");
        return response;
    }

    private PersonOption personOption(Person person) {
        PersonOption option = new PersonOption();
        option.id = person.getId();
        option.name = person.getName();
        option.label = option.id + ": " + option.name;
        option.eyeColor = person.getEyeColor() == null ? null : person.getEyeColor().name();
        option.hairColor = person.getHairColor() == null ? null : person.getHairColor().name();
        option.nationality = person.getNationality() == null ? null : person.getNationality().name();
        if (person.getBirthday() != null) {
            option.birthday = person.getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        Location location = person.getLocation();
        if (location != null) {
            option.locationId = location.getId();
            option.locationX = location.getX();
            option.locationY = location.getY();
            option.locationZ = location.getZ();
            option.locationName = location.getName();
        }
        return option;
    }

    private Dragon required(int id) {
        Dragon dragon = dragons.find(id);
        if (dragon == null) {
            throw new MissingException("Дракон не найден");
        }
        return dragon;
    }

    private void fill(Dragon dragon, DragonRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Нет данных дракона");
        }
        List<String> errors = new ArrayList<>();
        Coordinates coordinates = coordinates(request, errors);
        DragonCave cave = cave(request, errors);
        DragonHead head = head(request, errors);
        Person killer = killer(request, errors);
        dragon.setName(clean(request.name) == null ? "" : clean(request.name));
        Long age = longValue(request.age, "Возраст указан неверно", errors);
        dragon.setAge(age == null ? 0 : age);
        Float wingspan = floatValue(request.wingspan, "Размах крыльев указан неверно", errors);
        dragon.setWingspan(wingspan == null ? 0 : wingspan);
        dragon.setWeight(floatValue(request.weight, "Вес указан неверно", errors));
        dragon.setCharacter(character(request.character, errors));
        dragon.setCoordinates(coordinates);
        dragon.setCave(cave);
        dragon.setHead(head);
        dragon.setKiller(killer);
        if (dragon.getCreationDate() == null) {
            dragon.setCreationDate(LocalDate.now());
        }
        errors.addAll(messages(coordinates));
        errors.addAll(messages(cave));
        if (head != null) {
            errors.addAll(messages(head));
        }
        if (killer != null) {
            errors.addAll(messages(killer));
            if (killer.getLocation() != null) {
                errors.addAll(messages(killer.getLocation()));
            }
        }
        errors.addAll(messages(dragon));
        throwIfNeeded(errors);
        if (coordinates.getId() == null) {
            dragons.persist(coordinates);
        }
        if (cave.getId() == null) {
            dragons.persist(cave);
        }
        if (head != null && head.getId() == null) {
            dragons.persist(head);
        }
        if (killer != null) {
            if (killer.getLocation() != null && killer.getLocation().getId() == null) {
                dragons.persist(killer.getLocation());
            }
            if (killer.getId() == null) {
                dragons.persist(killer);
            }
        }
    }

    private Coordinates coordinates(DragonRequest request, List<String> errors) {
        Coordinates coordinates;
        Integer id = integer(request.coordinatesId, "Координаты указаны неверно", errors);
        if (id != null) {
            coordinates = dragons.coordinates(id);
            if (coordinates == null) {
                errors.add("Координаты не найдены");
                coordinates = new Coordinates();
            }
        } else {
            coordinates = new Coordinates();
        }
        Float x = floatValue(request.coordinatesX, "Координата X указана неверно", errors);
        if (clean(request.coordinatesX) == null) {
            errors.add("Укажите координату X");
        } else if (x != null) {
            coordinates.setX(x);
        }
        coordinates.setY(integer(request.coordinatesY, "Координата Y указана неверно", errors));
        return coordinates;
    }

    private DragonCave cave(DragonRequest request, List<String> errors) {
        DragonCave cave;
        Integer id = integer(request.caveId, "Пещера указана неверно", errors);
        if (id != null) {
            cave = dragons.cave(id);
            if (cave == null) {
                errors.add("Пещера не найдена");
                cave = new DragonCave();
            }
        } else {
            cave = new DragonCave();
        }
        Double depth = doubleValue(request.caveDepth, "Глубина пещеры указана неверно", errors);
        if (clean(request.caveDepth) == null) {
            errors.add("Укажите глубину пещеры");
        } else if (depth != null) {
            cave.setDepth(depth);
        }
        cave.setNumberOfTreasures(longValue(request.caveTreasures, "Число сокровищ указано неверно", errors));
        return cave;
    }

    private DragonHead head(DragonRequest request, List<String> errors) {
        if (clean(request.headId) == null && clean(request.headSize) == null) {
            return null;
        }
        DragonHead head;
        Integer id = integer(request.headId, "Голова указана неверно", errors);
        if (id != null) {
            head = dragons.head(id);
            if (head == null) {
                errors.add("Голова не найдена");
                head = new DragonHead();
            }
        } else {
            head = new DragonHead();
        }
        Long size = longValue(request.headSize, "Размер головы указан неверно", errors);
        if (clean(request.headSize) == null) {
            errors.add("Укажите размер головы");
        } else if (size != null) {
            head.setSize(size);
        }
        return head;
    }

    private Person killer(DragonRequest request, List<String> errors) {
        if (!killerRequested(request)) {
            return null;
        }
        Person person;
        Integer id = integer(request.killerId, "Убийца указан неверно", errors);
        if (id != null) {
            person = dragons.person(id);
            if (person == null) {
                errors.add("Убийца не найден");
                person = new Person();
            }
        } else {
            person = new Person();
        }
        String name = clean(request.killerName);
        person.setName(name == null ? "" : name);
        person.setEyeColor(color(request.eyeColor, errors, EYE));
        person.setHairColor(color(request.hairColor, errors, HAIR));
        person.setNationality(country(request.nationality, errors));
        person.setBirthday(birthday(request.birthday, errors));
        person.setLocation(location(request, errors));
        return person;
    }

    private boolean killerRequested(DragonRequest request) {
        return clean(request.killerId) != null
                || clean(request.killerName) != null
                || clean(request.eyeColor) != null
                || clean(request.hairColor) != null
                || clean(request.nationality) != null
                || clean(request.birthday) != null
                || clean(request.locationId) != null
                || clean(request.locationX) != null
                || clean(request.locationY) != null
                || clean(request.locationZ) != null
                || clean(request.locationName) != null;
    }

    private Location location(DragonRequest request, List<String> errors) {
        Location location;
        Integer id = integer(request.locationId, "Место указано неверно", errors);
        if (id != null) {
            location = dragons.location(id);
            if (location == null) {
                errors.add("Место не найдено");
                location = new Location();
            }
        } else {
            location = new Location();
        }
        Double x = doubleValue(request.locationX, "Координата X места указана неверно", errors);
        Double y = doubleValue(request.locationY, "Координата Y места указана неверно", errors);
        Float z = floatValue(request.locationZ, "Координата Z места указана неверно", errors);
        if (clean(request.locationX) == null) {
            errors.add("Укажите координату X места");
        } else if (x != null) {
            location.setX(x);
        }
        if (clean(request.locationY) == null) {
            errors.add("Укажите координату Y места");
        } else if (y != null) {
            location.setY(y);
        }
        if (clean(request.locationZ) == null) {
            errors.add("Укажите координату Z места");
        } else if (z != null) {
            location.setZ(z);
        }
        location.setName(clean(request.locationName));
        return location;
    }

    private DragonCharacter character(String value, List<String> errors) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return DragonCharacter.valueOf(clean);
        } catch (IllegalArgumentException exception) {
            errors.add(CHARACTER);
            return null;
        }
    }

    private Color color(String value, List<String> errors, String message) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return Color.valueOf(clean);
        } catch (IllegalArgumentException exception) {
            errors.add(message);
            return null;
        }
    }

    private Country country(String value, List<String> errors) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return Country.valueOf(clean);
        } catch (IllegalArgumentException exception) {
            errors.add(COUNTRY);
            return null;
        }
    }

    private LocalDateTime birthday(String value, List<String> errors) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(clean);
        } catch (DateTimeParseException exception) {
            errors.add("Дата рождения указана неверно");
            return null;
        }
    }

    private List<String> messages(Object entity) {
        List<String> result = new ArrayList<>();
        if (entity == null) {
            return result;
        }
        for (ConstraintViolation<Object> violation : validator.validate(entity)) {
            result.add(violation.getMessage());
        }
        return result;
    }

    private void throwIfNeeded(List<String> errors) {
        Set<String> unique = new LinkedHashSet<>();
        for (String error : errors) {
            if (error != null && !error.isBlank()) {
                unique.add(error);
            }
        }
        if (!unique.isEmpty()) {
            throw new IllegalArgumentException(String.join(". ", unique));
        }
    }

    private Integer integer(String value, String error, List<String> errors) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return Integer.valueOf(clean);
        } catch (NumberFormatException exception) {
            errors.add(error);
            return null;
        }
    }

    private Long longValue(String value, String error, List<String> errors) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return Long.valueOf(clean);
        } catch (NumberFormatException exception) {
            errors.add(error);
            return null;
        }
    }

    private Float floatValue(String value, String error, List<String> errors) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return Float.valueOf(clean.replace(',', '.'));
        } catch (NumberFormatException exception) {
            errors.add(error);
            return null;
        }
    }

    private Double doubleValue(String value, String error, List<String> errors) {
        String clean = clean(value);
        if (clean == null) {
            return null;
        }
        try {
            return Double.valueOf(clean.replace(',', '.'));
        } catch (NumberFormatException exception) {
            errors.add(error);
            return null;
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

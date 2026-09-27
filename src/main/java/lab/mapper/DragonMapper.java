package lab.mapper;

import lab.dto.DragonResponse;
import lab.entity.Dragon;
import lab.entity.DragonCave;
import lab.entity.DragonHead;
import lab.entity.Coordinates;
import lab.entity.Location;
import lab.entity.Person;

import java.time.format.DateTimeFormatter;

public final class DragonMapper {

    private DragonMapper() {
    }

    public static DragonResponse toResponse(Dragon dragon) {
        DragonResponse response = new DragonResponse();
        response.id = dragon.getId();
        response.name = dragon.getName();
        response.age = dragon.getAge();
        response.wingspan = dragon.getWingspan();
        response.weight = dragon.getWeight();
        response.character = dragon.getCharacter() == null ? null : dragon.getCharacter().name();
        if (dragon.getCreationDate() != null) {
            response.creationDate = dragon.getCreationDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        Coordinates coordinates = dragon.getCoordinates();
        if (coordinates != null) {
            response.coordinatesId = coordinates.getId();
            response.coordinatesX = coordinates.getX();
            response.coordinatesY = coordinates.getY();
        }
        DragonCave cave = dragon.getCave();
        if (cave != null) {
            response.caveId = cave.getId();
            response.caveDepth = cave.getDepth();
            response.caveTreasures = cave.getNumberOfTreasures();
        }
        DragonHead head = dragon.getHead();
        if (head != null) {
            response.headId = head.getId();
            response.headSize = head.getSize();
        }
        Person killer = dragon.getKiller();
        if (killer != null) {
            response.killerId = killer.getId();
            response.killerName = killer.getName();
            response.eyeColor = killer.getEyeColor() == null ? null : killer.getEyeColor().name();
            response.hairColor = killer.getHairColor() == null ? null : killer.getHairColor().name();
            response.nationality = killer.getNationality() == null ? null : killer.getNationality().name();
            if (killer.getBirthday() != null) {
                response.birthday = killer.getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            Location location = killer.getLocation();
            if (location != null) {
                response.locationId = location.getId();
                response.locationX = location.getX();
                response.locationY = location.getY();
                response.locationZ = location.getZ();
                response.locationName = location.getName();
            }
        }
        return response;
    }
}

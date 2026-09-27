package lab.dto;

import java.util.ArrayList;
import java.util.List;

public class OptionsResponse {
    public List<CoordinatesOption> coordinates = new ArrayList<>();
    public List<CaveOption> caves = new ArrayList<>();
    public List<PersonOption> persons = new ArrayList<>();
    public List<HeadOption> heads = new ArrayList<>();
    public List<LocationOption> locations = new ArrayList<>();
    public List<String> characters = new ArrayList<>();
    public List<String> colors = new ArrayList<>();
    public List<String> countries = new ArrayList<>();
}

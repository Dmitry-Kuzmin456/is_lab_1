package lab.dto;

import java.util.ArrayList;
import java.util.List;

public class TeamResponse {
    public Integer id;
    public String name;
    public Integer caveId;
    public String caveLabel;
    public List<String> members = new ArrayList<>();
}

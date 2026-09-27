package lab.dto;

import java.util.ArrayList;
import java.util.List;

public class PageResponse {
    public List<DragonResponse> items = new ArrayList<>();
    public int page;
    public int size;
    public long total;
}

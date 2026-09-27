package lab.repository;

import lab.entity.Dragon;

import java.util.ArrayList;
import java.util.List;

public class DragonPage {
    public final List<Dragon> items;
    public final long total;

    public DragonPage(List<Dragon> items, long total) {
        this.items = items;
        this.total = total;
    }

    public static DragonPage empty() {
        return new DragonPage(new ArrayList<>(), 0);
    }
}

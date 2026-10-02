package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Nation {
    private Map<Stat, Integer> stats = new HashMap<>();
    private String name;
    final private List<NationListener> listeners = new ArrayList<>();

    //Constructors
    public Nation() {
        this("Fantasia", 500, 20, 0);
    }
    public Nation(String name, int gold, int stability) {
        this(name, gold, stability, 0);
    }
    public Nation(String name, int gold, int stability, int relation) {
        this.name = name;
        stats.put(Stat.GOLD, gold);
        stats.put(Stat.STABILITY, stability);
        stats.put(Stat.RELATIONS, relation);
        stats.put(Stat.WOOD, 100);
        stats.put(Stat.STONE, 0);
        stats.put(Stat.FOOD, 0);
        stats.put(Stat.IRON, 20);
        stats.put(Stat.COAL, 0);
    }

    //General Getter - use for all getting (except for name)
    public int getValue(Stat stat) {
        return stats.get(stat);
    }
    //Exception - Name getter
    public String getName() {
        return name;
    }

    //General Setter - use for all setting
    public void setValue(Stat stat, int newValue) {
        if (stats.get(stat) != newValue) {
            stats.replace(stat, newValue);
            notifyListeners();
        }
    }

    //Listener Methods
    public void addListener(NationListener listener) {
        listeners.add(listener);
    }
    public void removeListener(NationListener listener) {
        listeners.remove(listener);
    }
    private void notifyListeners() {
        for (NationListener x : listeners) {
            x.onNationChanged(this);
        }
    }
}

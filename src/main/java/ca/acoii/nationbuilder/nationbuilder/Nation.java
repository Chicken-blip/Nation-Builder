package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.List;

public class Nation {
    private int gold,  stability, relation;
    private String name;
    final private List<NationListener> listeners = new ArrayList<>();

    //Constructors
    public Nation() {
        this("Fantasia", 100, 50, 0);
    }
    public Nation(String name, int gold, int stability) {
        this(name, gold, stability, 0);
    }
    public Nation(String name, int gold, int stability, int relation) {
        this.name = name;
        this.gold = gold;
        this.stability = stability;
        this.relation = relation;
    }

    //Getters
    public int getGold() {
        return gold;
    }
    public int getStability() {
        return stability;
    }
    public String getName() {
        return name;
    }
    public int getRelation() {
        return relation;
    }


    //Setters
    public void setGold(int gold) {
        if (this.gold != gold) {
            this.gold = gold;
            notifyListeners();
        }
    }

    public void setStability(int stability) {
        if (this.stability != stability) {
            this.stability = stability;
            notifyListeners();
        }
    }
    public void setRelation(int relation) {
        if (this.relation != relation) {
            this.relation = relation;
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

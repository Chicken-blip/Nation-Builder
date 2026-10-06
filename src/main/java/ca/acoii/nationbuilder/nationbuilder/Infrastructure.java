package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.List;

public class Infrastructure {
    private String name;
    private InfraCategory category;
    private List<EventEffect> cost;
    private int happiness, powerRequired, upkeep;

    public Infrastructure() {
        name = null;
        category = null;
        cost = new ArrayList<>();
        happiness = 0;
        powerRequired = 0;
        upkeep = 0;
    }

    public String getName() {
        return name;
    }

    public InfraCategory getCategory() {
        return category;
    }

    public List<EventEffect> getCost() {
        return cost;
    }

    public int getHappiness() {
        return happiness;
    }

    public int getPowerRequired() {
        return powerRequired;
    }

    public int getUpkeep() {
        return upkeep;
    }
}

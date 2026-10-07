package ca.acoii.nationbuilder.nationbuilder;

import java.util.Map;

public class Law {
    public static final double COST_CONSTANT = 3;
    private String name, description;
    private Map<BonusType, Double> bonuses;
    private Map<Stat, Integer> counterEffects;

    public Law() {
        name = null;
        bonuses = new java.util.HashMap<>();
        counterEffects = new java.util.HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<BonusType, Double> getBonuses() {
        return bonuses;
    }

    public Map<Stat, Integer> getCounterEffects() {
        return counterEffects;
    }
}

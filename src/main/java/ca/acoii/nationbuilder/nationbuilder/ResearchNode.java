package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchNode {
    private String id, name;
    private Map<BonusType, Double> bonuses;
    private int researchTime, startCost, maintenanceCost;
    private List<String> prerequisites;
    public ResearchNode() {
        id = null;
        name = null;
        bonuses = new HashMap<>();
        researchTime = 0;
        startCost = 0;
        maintenanceCost = 0;
        prerequisites = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<BonusType, Double> getBonuses() {
        return bonuses;
    }

    public int getResearchTime() {
        return researchTime;
    }

    public int getStartCost() {
        return startCost;
    }

    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }
}

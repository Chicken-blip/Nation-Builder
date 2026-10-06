package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.List;

public class Building {
    private String name;
    private List<EventEffect> cost;
    private List<EventEffect> output;
    private int powerRequired = 0, powerOutput = 0;
    private int upkeep;
    private BuildingSector buildingSector;

    public Building() {
        cost = new ArrayList<>();
        output = new ArrayList<>();
        name = null;
        buildingSector = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EventEffect> getCost() {
        return cost;
    }

    public void setCost(List<EventEffect> cost) {
        this.cost = cost;
    }

    public List<EventEffect> getOutput() {
        return output;
    }

    public void setOutput(List<EventEffect> output) {
        this.output = output;
    }

    public int getPowerRequired() {
        return powerRequired;
    }

    public void setPowerRequired(int powerRequired) {
        this.powerRequired = powerRequired;
    }

    public int getPowerOutput() {
        return powerOutput;
    }

    public void setPowerOutput(int powerOutput) {
        this.powerOutput = powerOutput;
    }

    public void setUpkeep(int upkeep) {
        this.upkeep = upkeep;
    }

    public int getUpkeep() {
        return upkeep;
    }

    public BuildingSector getBuildingSector() {
        return buildingSector;
    }
    public void setSector(BuildingSector sector) {
        this.buildingSector = sector;
    }
}

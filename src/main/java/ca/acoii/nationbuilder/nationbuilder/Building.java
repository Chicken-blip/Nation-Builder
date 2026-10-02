package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.List;

public class Building {
    private String name;
    private List<EventEffect> cost;
    private List<EventEffect> output;

    public Building() {
        cost = new ArrayList<>();
        output = new ArrayList<>();
        name = null;
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
}

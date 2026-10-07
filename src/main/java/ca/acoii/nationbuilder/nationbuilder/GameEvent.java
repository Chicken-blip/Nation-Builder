package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.List;

public class GameEvent {
    private int id;
    private String text;
    private EventType eventType;
    private Priority priority;
    private double chance;
    private List<Condition> conditions;
    private List<EventOption> options;

    public GameEvent() {
        conditions = new ArrayList<>();
        options = new ArrayList<>();
        chance = 0;
        id = 0;
        text = "";
        priority = null;
        eventType = EventType.GENERIC;
    }

    //Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public void setChance(double chance) {
        this.chance = chance;
    }
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
    public void setOptions(List<EventOption> options) {
        this.options = options;
    }

    //Getters

    public int getId() {
        return id;
    }
    public String getText() {
        return text;
    }
    public Priority getPriority() {
        return priority;
    }
    public double getChance() {
        return chance;
    }
    public List<Condition> getConditions() {
        return conditions;
    }
    public List<EventOption> getOptions() {
        return options;
    }

    public EventType getEventType() {
        return eventType;
    }
}

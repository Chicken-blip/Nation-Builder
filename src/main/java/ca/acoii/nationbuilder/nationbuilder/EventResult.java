package ca.acoii.nationbuilder.nationbuilder;

import java.util.ArrayList;
import java.util.List;

public class EventResult {
    private String narrative;
    private List<EventEffect> effects;
    public EventResult() {
        effects = new ArrayList<>();
        narrative = "";
    }

    public EventResult(ArrayList<EventEffect> effects) {
        this.effects = effects;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public void setEffects(List<EventEffect> effects) {
        this.effects = effects;
    }

    public String getNarrative() {
        return narrative;
    }

    public List<EventEffect> getEffects() {
        return effects;
    }
}

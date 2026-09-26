package ca.acoii.nationbuilder.nationbuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private final Nation playerNation;
    private int turn;
    private final List<GameEngineListener> listeners = new ArrayList<>();
    private List<GameEvent> events;
    private List<Nation> otherNations = new ArrayList<>();
    public GameEngine(Nation nation) {
        playerNation = nation;
        turn = 1;
        otherNations.add(new Nation("Maxira", 500, 50, 0));
        otherNations.add(new Nation("Xenos", 300, 50, 0));
    }
    public void nextTurn() {
        adjustStat(Stat.GOLD, -10);
        turn++;
        notifyListeners();
    }

    private void adjustStat(Stat stat, int change) {
        playerNation.setValue(stat,playerNation.getValue(Stat.GOLD) + change);
    }

    public int getTurn() {
        return turn;
    }
    public void addListener(GameEngineListener listener) {
        listeners.add(listener);
    }
    public void removeListener(GameEngineListener listener) {
        listeners.remove(listener);
    }
    private void notifyListeners() {
        for (GameEngineListener x : listeners) {
            x.onTurnAdvanced(this);
        }
    }

    public void loadEvents() {
        events = new EventLoader().loadAll();
    }

    public List<GameEvent> getEventList() {
        return this.events;
    }

    private boolean evaluateCondition(Condition c, Nation n) {
        Nation nation = null;
        switch (c.getTarget()) {
            case SOURCE -> nation = n;
            case PLAYER ->  nation = playerNation;
        }
        int stat = playerNation.getValue(c.getStat());
        switch (c.getOp()) {
            case EQUAL -> {
                return stat == c.getValue();
            }
            case LESS_THAN -> {
                return stat < c.getValue();
            }
            case NOT_EQUAL -> {
                return stat != c.getValue();
            }
            case GREATER_THAN -> {
                return stat > c.getValue();
            }
            case LESS_OR_EQUAL -> {
                return stat <= c.getValue();
            }
            case GREATER_OR_EQUAL -> {
                return stat >= c.getValue();
            }
        }
        return false;
    }
    private boolean evaluateAll(List<Condition> conditions, Nation n) {
        for (Condition c : conditions) {
            if (!evaluateCondition(c, n)) {return false;}
        }
        return true;
    }

    private Nation findNation(GameEvent evt) {
        List<Nation> elligibleNations = new ArrayList<>();
        for (Nation candidate : otherNations) {
            if (evaluateAll(evt.getConditions(), candidate)) elligibleNations.add(candidate);
        }
        if (elligibleNations.isEmpty()) return null;
        return elligibleNations.get(new Random().nextInt(elligibleNations.size())); //Picks a random nation from the nations elligible on the event conditions
    }

    public FiredEvent fireEvent() {
        for (GameEvent evt : events) {
            Nation nation = findNation(evt);
            if (nation == null) continue;
            if (Math.random() < evt.getChance()) {
                return new FiredEvent(evt, nation);
            }
        }
        return null;
    }

}

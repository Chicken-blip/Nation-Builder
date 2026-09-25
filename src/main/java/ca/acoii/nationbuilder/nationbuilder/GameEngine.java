package ca.acoii.nationbuilder.nationbuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private final Nation playerNation;
    private int turn;
    private final List<GameEngineListener> listeners = new ArrayList<>();
    private List<GameEvent> events;
    public GameEngine(Nation nation) {
        playerNation = nation;
        turn = 1;
    }
    public void nextTurn() {
        adjustGold();
        adjustStability();
        turn++;
        notifyListeners();
    }

    private void adjustStability() {
        int stabIncrement = 0;
        if (playerNation.getStability() <= 35) {
            stabIncrement = (int) (Math.random() * 6) - 5;
        }
        playerNation.setStability(playerNation.getStability() + stabIncrement);
    }

    private void adjustGold() {
        playerNation.setGold(playerNation.getGold() - 50);
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

    public int getEventListSize() {
        return this.events.size();
    }


}

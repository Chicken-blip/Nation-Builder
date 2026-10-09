package ca.acoii.nationbuilder.nationbuilder;

public class FiredEvent {
    GameEvent evt;
    Nation n;
    public FiredEvent() {

    }
    public FiredEvent(GameEvent evt, Nation n) {
        this.evt = evt;
        this.n = n;
    }

    public FiredEvent(FiredEvent e) {
        this.evt = new GameEvent(e.evt);
        this.n = e.n;
    }

    public GameEvent getEvt() {
        return evt;
    }

    public Nation getNation() {
        return n;
    }
}

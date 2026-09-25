package ca.acoii.nationbuilder.nationbuilder;

public class EventEffect {
    private Stat stat;
    private int delta;

    public EventEffect() {
        stat = null;
        delta = 0;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public Stat getStat() {
        return stat;
    }

    public int getDelta() {
        return delta;
    }
}

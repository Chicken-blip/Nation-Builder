package ca.acoii.nationbuilder.nationbuilder;

public class EventEffect {
    private Stat stat;
    private double delta;

    public EventEffect() {
        stat = null;
        delta = 0;
    }

    public EventEffect(Stat stat, double v) {
        this.stat = stat;
        this.delta = v;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public Stat getStat() {
        return stat;
    }

    public double getDelta() {
        return delta;
    }
}

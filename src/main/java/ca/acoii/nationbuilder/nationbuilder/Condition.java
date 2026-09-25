package ca.acoii.nationbuilder.nationbuilder;

public class Condition {
    private Target target;
    private Stat stat;
    private Op op;
    private int value;
    public Condition() {
        target = null;
        stat = null;
        op = null;
        value = 0;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Target getTarget() {
        return target;
    }

    public Stat getStat() {
        return stat;
    }

    public Op getOp() {
        return op;
    }

    public int getValue() {
        return value;
    }
}

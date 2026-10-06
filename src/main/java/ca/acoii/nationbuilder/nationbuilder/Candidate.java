package ca.acoii.nationbuilder.nationbuilder;

public class Candidate {
    private String name;
    private double bonusPercent;
    private int upkeep;
    public Candidate() {
        name = null;
        bonusPercent = 0;
        upkeep = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBonusPercent() {
        return bonusPercent;
    }

    public void setBonusPercent(double bonusPercent) {
        this.bonusPercent = bonusPercent;
    }

    public int getUpkeep() {
        return upkeep;
    }

    public void setUpkeep(int upkeep) {
        this.upkeep = upkeep;
    }
}

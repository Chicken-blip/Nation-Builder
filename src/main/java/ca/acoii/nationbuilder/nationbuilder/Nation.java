package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Nation {
    private Map<Stat, Integer> stats = new HashMap<>();
    private double gold;
    private String name;
    final private List<NationListener> listeners = new ArrayList<>();
    private Map<Stat, Double> sellPrices, buyPrices;
    private List<AgreementType> agreements = new ArrayList<>();
    private Map<UnitType, Integer> military = new HashMap<>();
    private RationSetting rationSetting = RationSetting.NONE;
    private BirthRateSetting birthRateSetting = BirthRateSetting.NEUTRAL;
    private boolean immigrationOpen;
    private double immigrationRate = 0.0;

    //Constructors
    public Nation() {
        this("Fantasia", 500, 100, 0);
    }
    public Nation(String name, int gold, int stability) {
        this(name, gold, stability, 0);
    }
    public Nation(String name, int gold, int stability, int relation) {
        this.name = name;
        this.gold = gold;
        stats.put(Stat.STABILITY, stability);
        stats.put(Stat.RELATIONS, relation);
        stats.put(Stat.WOOD, 100);
        stats.put(Stat.STONE, 0);
        stats.put(Stat.CLOTH, 0);
        stats.put(Stat.IRON, 20);
        stats.put(Stat.COAL, 0);
        stats.put(Stat.FOOD, 300);
        stats.put(Stat.POPULATION, 2000);
    }
    //Jackson Constructor
    @JsonCreator
    public Nation(@JsonProperty("name") String name, @JsonProperty("gold") double gold, @JsonProperty("stability") int stability, @JsonProperty("relations") int relations, @JsonProperty("buyPrices") Map<Stat, Double> buyPrices, @JsonProperty("sellPrices") Map<Stat, Double> sellPrices, @JsonProperty("military") Map<UnitType, Integer> military) {
        this.name = name;
        this.buyPrices = buyPrices;
        this.sellPrices = sellPrices;
        this.military = military;
        setValue(Stat.GOLD, gold);
        setValue(Stat.STABILITY, stability);
        setValue(Stat.RELATIONS, relations);
    }

    //General Getter - use for all getting (except for name)
    public int getValue(Stat stat) {
        if (stat == Stat.GOLD) {
            return (int) gold;
        } else {
            return stats.getOrDefault(stat, 0);
        }
    }
    public double getValue(Stat stat, int i) {
        if (stat == Stat.GOLD) {
            return gold;
        } else {
            return stats.getOrDefault(stat, 0);
        }
    }

    //Exception - Name getter
    public String getName() {
        return name;
    }

    //General Setter - use for all setting
    public void setValue(Stat stat, int newValue) {
        if (stat == Stat.GOLD) {
            gold = newValue;
            notifyListeners();
        }
        if (stats.getOrDefault(stat, 0) != newValue) {
            stats.put(stat, newValue);
            notifyListeners();
        }
    }

    public void setValue(Stat stat, double newValue) {
        if (stat == Stat.GOLD) {
            gold = newValue;
            notifyListeners();
        } else {
            setValue(stat, (int) newValue);
        }
    }

    public boolean hasAgreement(AgreementType a) {
        return agreements.contains(a);
    }
    public void addAgreement(AgreementType a) {
        agreements.add(a);
    }
    public void removeAgreement(AgreementType a) {
        agreements.remove(a);
    }
    public List<AgreementType> getAgreements() {
        return agreements;
    }

    public Double buyPrice(Stat s) {return buyPrices.getOrDefault(s, 0.0);}
    public Double sellPrice(Stat s) {return sellPrices.getOrDefault(s, 0.0);}

    public RationSetting getRationSetting() {
        return rationSetting;
    }

    public void setRationSetting(RationSetting rationSetting) {
        this.rationSetting = rationSetting;
    }

    public BirthRateSetting getBirthRateSetting() {
        return birthRateSetting;
    }
    public void setBirthRateSetting(BirthRateSetting birthRateSetting) {
        this.birthRateSetting = birthRateSetting;
    }

    public void toggleImmigration() {
        immigrationOpen = !immigrationOpen;
    }
    public boolean isImmigrationOpen() {
        return immigrationOpen;
    }

    public void setImmigrationRate(double immigrationRate) {
        this.immigrationRate = immigrationRate;
    }
    public double getImmigrationRate() {
        return immigrationRate;
    }
    public Map<UnitType, Integer> getMilitary() {
        return military;
    }

    //Listener Methods
    public void addListener(NationListener listener) {
        listeners.add(listener);
    }
    public void removeListener(NationListener listener) {
        listeners.remove(listener);
    }
    private void notifyListeners() {
        for (NationListener x : listeners) {
            x.onNationChanged(this);
        }
    }
}

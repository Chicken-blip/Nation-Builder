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
    public Nation(@JsonProperty("name") String name, @JsonProperty("gold") double gold, @JsonProperty("stability") int stability, @JsonProperty("relations") int relations, @JsonProperty("buyPrices") Map<Stat, Double> buyPrices, @JsonProperty("sellPrices") Map<Stat, Double> sellPrices) {
        this.name = name;
        this.buyPrices = buyPrices;
        this.sellPrices = sellPrices;
        setGold(gold);
        setValue(Stat.STABILITY, stability);
        setValue(Stat.RELATIONS, relations);
    }

    //General Getter - use for all getting (except for name)
    public int getValue(Stat stat) {
        return stats.getOrDefault(stat, 0);
    }
    public double getGold() {
        return gold;
    }
    //Exception - Name getter
    public String getName() {
        return name;
    }

    //General Setter - use for all setting
    public void setValue(Stat stat, int newValue) {
        if (stats.getOrDefault(stat, 0) != newValue) {
            stats.put(stat, newValue);
            notifyListeners();
        }
    }

    public void setGold(double newValue) {
        //Only applies for gold
        this.gold = newValue;
        notifyListeners();
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

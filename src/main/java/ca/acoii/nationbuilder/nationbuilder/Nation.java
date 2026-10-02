package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Nation {
    private Map<Stat, Integer> stats = new HashMap<>();
    private String name;
    final private List<NationListener> listeners = new ArrayList<>();
    private Map<Stat, Integer> sellPrices, buyPrices;
    private Set<AgreementType> agreements = new HashSet<>();

    //Constructors
    public Nation() {
        this("Fantasia", 500, 20, 0);
    }
    public Nation(String name, int gold, int stability) {
        this(name, gold, stability, 0);
    }
    public Nation(String name, int gold, int stability, int relation) {
        this.name = name;
        stats.put(Stat.GOLD, gold);
        stats.put(Stat.STABILITY, stability);
        stats.put(Stat.RELATIONS, relation);
        stats.put(Stat.WOOD, 100);
        stats.put(Stat.STONE, 0);
        stats.put(Stat.FOOD, 0);
        stats.put(Stat.IRON, 20);
        stats.put(Stat.COAL, 0);
    }
    //Jackson Constructor
    @JsonCreator
    public Nation(@JsonProperty("name") String name, @JsonProperty("gold") int gold, @JsonProperty("stability") int stability, @JsonProperty("relations") int relations, @JsonProperty("buyPrices") Map<Stat, Integer> buyPrices, @JsonProperty("sellPrices") Map<Stat, Integer> sellPrices) {
        this.name = name;
        this.buyPrices = buyPrices;
        this.sellPrices = sellPrices;
        setValue(Stat.GOLD, gold);
        setValue(Stat.STABILITY, stability);
        setValue(Stat.RELATIONS, relations);
    }

    //General Getter - use for all getting (except for name)
    public int getValue(Stat stat) {
        return stats.getOrDefault(stat, 0);
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

    public boolean hasAgreement(AgreementType a) {
        return agreements.contains(a);
    }
    public void addAgreement(AgreementType a) {
        agreements.add(a);
    }
    public void removeAgreement(AgreementType a) {
        agreements.remove(a);
    }

    public int buyPrice(Stat s) {return buyPrices.get(s);}
    public int sellPrice(Stat s) {return sellPrices.get(s);}

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

package ca.acoii.nationbuilder.nationbuilder;

import java.io.IOException;
import java.util.*;

public class GameEngine {
    private final Nation playerNation;
    private int turn;
    private final List<GameEngineListener> listeners = new ArrayList<>();
    private List<GameEvent> events;
    private List<Building> buildings;
    private List<Nation> otherNations = new ArrayList<>();
    private Map<String, Integer> builtBuildings = new HashMap<>();
    private Map<Stat, Double> consumables = new HashMap<>();
    public GameEngine(Nation nation) {
        playerNation = nation;
        consumables.put(Stat.FOOD, 0.01);
        consumables.put(Stat.CLOTH, 0.002);
        builtBuildings.put("Power Plant", 1);
        turn = 1;
    }
    public List<Nation> getOtherNations() {
        return otherNations;
    }
    public void nextTurn() {
        adjustStat(Stat.GOLD,-10);
        applyBuildingOutput();
        populationEffects();
        for (Nation n : otherNations) {
            if (n.getValue(Stat.RELATIONS) < 0) {
                n.removeAgreement(AgreementType.TRADE);
            }
        }
        turn++;
        notifyListeners();
    }

    private void applyBuildingOutput() {
        int capacity = getPowerSupply() - getPowerDemand();
        for (String s : builtBuildings.keySet()) {
            Building b = getBuilding(s);
            if (b.getPowerRequired() > 0 && capacity < 0) continue;
            int count = builtBuildings.get(s);
            for (EventEffect e : b.getOutput()) {
                for (int i = 0; i < count; i++) {
                    applyEffect(e);
                }
            }
        }
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
        for (GameEvent e : events) {
            System.out.println("Loaded: id=" + e.getId() + " chance=" + e.getChance() +
                    " text=" + e.getText().substring(0, Math.min(40, e.getText().length())));
        }
    }

    public List<GameEvent> getEventList() {
        return this.events;
    }

    public void loadBuildings() {
        buildings = new BuildingLoader().loadAll();
        for (Building b : buildings) {
            System.out.println("Building: " + b.getName());
        }
    }

    public void loadNations() {
        otherNations = new NationLoader().loadAll();
        for (Nation n : otherNations) {
            System.out.println("Nation: " + n.getName());
        }
    }

    private boolean evaluateCondition(Condition c, Nation n) {
        Nation nation = null;
        switch (c.getTarget()) {
            case SOURCE -> nation = n;
            case PLAYER ->  nation = playerNation;
        }
        int stat = nation.getValue(c.getStat());
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

    private boolean evaluateCost(EventEffect e) {
        return playerNation.getValue(e.getStat()) >= Math.abs(e.getDelta());
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

    public boolean build(String building) {
        Building b = getBuilding(building);
        if (b != null) {
                //Checks each cost
                for (EventEffect e : b.getCost()) {
                    if (!evaluateCost(e)) {
                        return false;
                    }
                }
                //Deducts the cost
                for (EventEffect e : b.getCost()) {
                    applyEffect(e);
                }
                //Increments this building's count
                builtBuildings.put(b.getName(), builtBuildings.getOrDefault(b.getName(), 0) + 1);
                notifyListeners();
                return true;
        }
        notifyListeners();
        return false;
    }

    public void establishTradeAgreement(Nation n) {
        n.addAgreement(AgreementType.TRADE);
    }

    public boolean buyResource(Nation seller, Stat resource, int qty) {
        if (!seller.hasAgreement(AgreementType.TRADE)) return false;
        int cost = seller.buyPrice(resource) * qty;
        if (playerNation.getValue(Stat.GOLD) < cost) return false;
        playerNation.setValue(Stat.GOLD, playerNation.getValue(Stat.GOLD) - cost);
        playerNation.setValue(resource, playerNation.getValue(resource) + qty);
        return true;
    }

    public boolean sellResource(Nation buyer, Stat resource, int qty) {
        if (!buyer.hasAgreement(AgreementType.TRADE)) return false;
        int revenue = buyer.sellPrice(resource) * qty;
        if (playerNation.getValue(resource) < qty) return false;
        playerNation.setValue(Stat.GOLD, playerNation.getValue(Stat.GOLD) + revenue);
        playerNation.setValue(resource, playerNation.getValue(resource) - qty);
        return true;
    }

    private void applyEffect(EventEffect e) {
        playerNation.setValue(e.getStat(), playerNation.getValue(e.getStat()) + e.getDelta());
    }

    private Building getBuilding(String n) {
        for (Building b : buildings) {
            if (Objects.equals(b.getName(), n)) {
                return b;
            }
        }
        return null;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public int getBuildingCount(Building b) {
        return builtBuildings.getOrDefault(b.getName(), 0);
    }

    public void populationEffects() {
        int pop = playerNation.getValue(Stat.POPULATION);
        int shortages = 0;
        // Consumable changes
        for (Stat s : consumables.keySet()) {
            int actualComp = (int) (playerNation.getValue(s) - (consumables.get(s) * playerNation.getValue(Stat.POPULATION)));

            //Check if there's a shortage
            if (actualComp < 0) {
                shortages++;
            }

            //Sets the value to either the amount after consumption, or 0 if there's a shortage
            playerNation.setValue(s, Integer.max(actualComp, 0));
        }

        //Population changes!
        if (shortages == 0) {
            //No shortages - population grows
            playerNation.setValue(Stat.POPULATION,playerNation.getValue(Stat.POPULATION) + (pop / 100));
        } else {
            //Shortages exist - population declines
            playerNation.setValue(Stat.POPULATION,playerNation.getValue(Stat.POPULATION) - (pop / 20));
        }

        //Stability changes for extreme starvation
        if (shortages > 1) {
            playerNation.setValue(Stat.STABILITY, playerNation.getValue(Stat.STABILITY) - 5);
        }
    }

    public int getPowerSupply() {
        int sum = 0;
        if (builtBuildings.isEmpty()) return 0;
        for (String b: builtBuildings.keySet()) {
            sum += (getBuilding(b).getPowerOutput()) * builtBuildings.get(b);
        }
        return sum;
    }
    public int getPowerDemand() {
        int sum = 0;
        if (builtBuildings.isEmpty()) return 0;
        for (String b: builtBuildings.keySet()) {
            sum += (getBuilding(b).getPowerRequired()) * builtBuildings.get(b);
        }
        return sum;
    }


}

package ca.acoii.nationbuilder.nationbuilder;

import java.util.*;

public class GameEngine {
    private final Nation playerNation;
    private int turn;
    private final List<GameEngineListener> listeners = new ArrayList<>();
    private List<GameEvent> events;
    private List<Building> buildings;
    private List<Infrastructure> infrastructures;
    private List<Nation> otherNations = new ArrayList<>();
    private final Map<String, Integer> builtBuildings = new HashMap<>();
    private final Map<String, Integer> builtInfrastructure = new HashMap<>();
    private final Map<Stat, Double> consumables = new HashMap<>();
    private double taxRate;
    private boolean starving;
    public GameEngine(Nation nation) {
        playerNation = nation;
        consumables.put(Stat.FOOD, 0.01);
        consumables.put(Stat.CLOTH, 0.002);
        builtBuildings.put("Power Plant", 1);
        builtInfrastructure.put("Elementary School", 1);
        builtInfrastructure.put("Clinic", 1);
        builtInfrastructure.put("Park", 1);
        taxRate = 0.2;
        starving = false;
        turn = 1;
    }
    public List<Nation> getOtherNations() {
        return otherNations;
    }
    public void nextTurn() {
        applyBuildingUpkeep();
        applyBuildingOutput();
        populationEffects();
        applyTaxes();
        applyInfraEffects();
        for (Nation n : otherNations) {
            if (n.getValue(Stat.RELATIONS) < 0) {
                n.removeAgreement(AgreementType.TRADE);
            }
        }
        turn++;
        notifyListeners();
    }

    private void applyBuildingUpkeep() {
        for (String str : builtBuildings.keySet()) {
            Building b = getBuilding(str);
            assert b != null;
            playerNation.setGold((-b.getUpkeep() * getBuildingCount(b)) + playerNation.getGold());
        }
        for (String str : builtInfrastructure.keySet()) {
            Infrastructure i = getInfrastructure(str);
            assert i != null;
            playerNation.setGold((-i.getUpkeep() * getInfrastructureCount(i)) + playerNation.getGold());
        }
    }

    private void applyBuildingOutput() {
        int capacity = getPowerSupply() - getPowerDemand();
        for (String s : builtBuildings.keySet()) {
            Building b = getBuilding(s);
            assert b != null;
            if (b.getPowerRequired() > 0 && capacity < 0) continue;
            int count = builtBuildings.get(s);
            for (EventEffect e : b.getOutput()) {
                for (int i = 0; i < count; i++) {
                    applyEffect(e);
                }
            }
        }

    }

    private void applyInfraEffects() {
        for (InfraCategory e : InfraCategory.values()) {
            if (getInfraHappiness(e) < getHappinessRequired(e)) {
                adjustStat(Stat.STABILITY,-1);
            }
        }
    }

    private void adjustStat(Stat stat, int change) {
        playerNation.setValue(stat, playerNation.getValue(stat) + change);
    }

    private void adjustStat(Stat stat, double change) {
        playerNation.setGold( playerNation.getGold() + change);
    }

    public int getTurn() {
        return turn;
    }
    public void addListener(GameEngineListener listener) {
        listeners.add(listener);
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

    public void loadInfrastructure() {
        infrastructures = new InfrastructureLoader().loadAll();
        for (Infrastructure i : infrastructures) {
            System.out.println("Infrastructure: " + i.getName());
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

    public boolean build(String name, int type) {
        switch (type) {
            case 1 -> {
                //Given a building name
                Building b = getBuilding(name);
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
            case 2 -> {
                //Given an infrastructure name
                Infrastructure i = getInfrastructure(name);
                if (i != null) {
                    //Checks each cost
                    for (EventEffect e : i.getCost()) {
                        if (!evaluateCost(e)) {
                            return false;
                        }
                    }
                    //Deducts the cost
                    for (EventEffect e : i.getCost()) {
                        applyEffect(e);
                    }
                    //Increments this infrastructure's count
                    builtInfrastructure.put(i.getName(), builtInfrastructure.getOrDefault(i.getName(), 0) + 1);
                    notifyListeners();
                    return true;
                }
                notifyListeners();
                return false;
            }
        }
        return false;
    }




    public void establishTradeAgreement(Nation n) {
        n.addAgreement(AgreementType.TRADE);
    }

    public boolean buyResource(Nation seller, Stat resource, int qty) {
        if (!seller.hasAgreement(AgreementType.TRADE)) return false;
        double cost = seller.buyPrice(resource) * qty;
        if (playerNation.getGold() < cost) return false;
        playerNation.setGold(playerNation.getGold() - cost);
        adjustStat(resource, qty);
        return true;
    }

    public boolean sellResource(Nation buyer, Stat resource, int qty) {
        if (!buyer.hasAgreement(AgreementType.TRADE)) return false;
        double revenue = buyer.sellPrice(resource) * qty;
        if (playerNation.getValue(resource) < qty) return false;
        playerNation.setGold(playerNation.getGold() + revenue);
        adjustStat(resource, -qty);
        return true;
    }

    private void applyEffect(EventEffect e) {
        adjustStat(e.getStat(), e.getDelta());
    }

    private Building getBuilding(String n) {
        for (Building b : buildings) {
            if (Objects.equals(b.getName(), n)) {
                return b;
            }
        }
        return null;
    }

    private Infrastructure getInfrastructure(String n) {
        for (Infrastructure i : infrastructures) {
            if (Objects.equals(i.getName(), n)) {
                return i;
            }
        }
        return null;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public List<Infrastructure> getInfrastructure() {
        return infrastructures;
    }

    public int getBuildingCount(Building b) {
        return builtBuildings.getOrDefault(b.getName(), 0);
    }
    public int getInfrastructureCount(Infrastructure i) {
        return builtInfrastructure.getOrDefault(i.getName(), 0);
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
            adjustStat(Stat.POPULATION, pop / 50);
        } else {
            //Shortages exist - population declines
            adjustStat(Stat.POPULATION, -(pop / 20));
        }

        starving = shortages > 1;

        //Stability changes for extreme starvation
        if (starving) {
            adjustStat(Stat.STABILITY, -5);
        }
    }

    public void applyTaxes() {
        int pop = playerNation.getValue(Stat.POPULATION);
        int revenue = (int) (pop * taxRate * 0.2);
        playerNation.setGold(playerNation.getGold() + revenue);

        double threshold = 0.4;
        if (starving) threshold = 0.25;

        if (taxRate > threshold) {
            adjustStat(Stat.STABILITY, -1);
        }
    }

    public int getPowerSupply() {
        int sum = 0;
        if (builtBuildings.isEmpty()) return 0;
        for (String b: builtBuildings.keySet()) {
            assert getBuilding(b) != null;
            sum += (getBuilding(b).getPowerOutput()) * builtBuildings.get(b);
        }
        return sum;
    }
    public int getPowerDemand() {
        int sum = 0;
        if (builtBuildings.isEmpty()) return 0;
        for (String b: builtBuildings.keySet()) {
            assert getBuilding(b) != null;
            sum += (getBuilding(b).getPowerRequired()) * builtBuildings.get(b);
        }
        for (String i : builtInfrastructure.keySet()) {
            assert getInfrastructure(i) != null;
            sum += (getInfrastructure(i).getPowerRequired()) * builtInfrastructure.get(i);
        }
        return sum;
    }

    public int getInfraHappiness(InfraCategory e) {
        int sum = 0;
        for (String n : builtInfrastructure.keySet()) {
            Infrastructure i = getInfrastructure(n);
            assert i != null;
            if (i.getCategory() == e) {
                sum += i.getHappiness() * builtInfrastructure.get(i.getName());
            }
        }
        return sum;
    }

    public int getHappinessRequired(InfraCategory e) {
        return (int) (playerNation.getValue(Stat.POPULATION) * 0.0015);
    }

    public double getTaxRate() {
        return taxRate;
    }

    public boolean isStarving() {
        return starving;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }
}

package ca.acoii.nationbuilder.nationbuilder;

import java.util.*;

public class GameEngine {
    private final Nation playerNation;
    private int turn;
    private final List<GameEngineListener> listeners = new ArrayList<>();
    private List<GameEvent> events;
    private List<Building> buildings;
    private List<Infrastructure> infrastructures;
    private List<Law> availableLaws;
    private Law currentLaw = null;
    private int lawCooldown = 0;
    private Map<Role, List<Candidate>> roleCandidates;
    private List<Nation> otherNations = new ArrayList<>();
    private final Map<String, Integer> builtBuildings = new HashMap<>();
    private final Map<String, Integer> builtInfrastructure = new HashMap<>();
    private final Map<Role, Candidate> appointedRoles = new HashMap<>();
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
        applyAppointeeUpkeep();
        for (Nation n : otherNations) {
            if (n.getValue(Stat.RELATIONS) < 0) {
                n.removeAgreement(AgreementType.TRADE);
            }
        }
        if (lawCooldown > 0) lawCooldown--;
        if (currentLaw != null) applyLawEffects();
        if (playerNation.getValue(Stat.STABILITY) > 100) playerNation.setValue(Stat.STABILITY,100);
        turn++;
        notifyListeners();
    }

    private void applyBuildingUpkeep() {
        for (String str : builtBuildings.keySet()) {
            Building b = getBuilding(str);
            assert b != null;
            playerNation.setValue(Stat.GOLD, (-b.getUpkeep() * getBuildingCount(b)) + playerNation.getValue(Stat.GOLD));
        }
        for (String str : builtInfrastructure.keySet()) {
            Infrastructure i = getInfrastructure(str);
            assert i != null;
            playerNation.setValue(Stat.GOLD, (-i.getUpkeep() * getInfrastructureCount(i)) + playerNation.getValue(Stat.GOLD));
        }
    }

    private void applyAppointeeUpkeep() {        for (Role r : appointedRoles.keySet()) {
            Candidate c = appointedRoles.get(r);
            assert c != null;
            playerNation.setValue(Stat.GOLD, (-c.getUpkeep()) + playerNation.getValue(Stat.GOLD));
        }
    }

    private void applyBuildingOutput() {
        double capacity = getPowerSupply() - getPowerDemand();
        for (String s : builtBuildings.keySet()) {
            Building b = getBuilding(s);
            assert b != null;
            if (b.getPowerRequired() > 0 && capacity < 0) continue;
            int count = builtBuildings.get(s);
            for (EventEffect e : b.getOutput()) {
                for (int i = 0; i < count; i++) {
                    EventEffect a;
                    if (b.getBuildingSector() == BuildingSector.INDUSTRY) {
                        a = new EventEffect(e.getStat(), e.getDelta() * getAppointeeBonus(BonusType.INDUSTRY_OUTPUT));
                        applyEffect(a);
                    } else if (b.getBuildingSector() == BuildingSector.AGRICULTURE) {
                        a = new EventEffect(e.getStat(), e.getDelta() * getAppointeeBonus(BonusType.AGRICULTURE_OUTPUT));
                        applyEffect(a);
                    } else {
                        applyEffect(e);
                    }
                }
            }
        }

    }

    private void applyInfraEffects() {
        int adjustments = 0;
        for (InfraCategory e : InfraCategory.values()) {
            if (getInfraHappiness(e) < getHappinessRequired(e)) {
                adjustments--;
            }
        }
        if (adjustments == 0) {adjustments = 1;}
        adjustStat(Stat.STABILITY, adjustments);
    }

    private void adjustStat(Stat stat, int change) {
        playerNation.setValue(stat, playerNation.getValue(stat) + change);
    }

    private void adjustStat(Stat stat, double change) {
        playerNation.setValue( stat, playerNation.getValue(stat) + change);
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

    public void loadAppointees() {
        roleCandidates = new HashMap<>();
        List<RoleFile> files = new AppointeeLoader().loadAll();
        for (RoleFile x : files) {
            roleCandidates.put(x.role, x.candidates);
        }
    }

    public void loadLaws() {
        availableLaws = new LawLoader().loadAll();
    }

    public boolean enactLaw(Law law) {
        if (lawCooldown > 0) return false;
        if (playerNation.getValue(Stat.GOLD) < getLawCost()) {
            return false;
        }
        if (currentLaw != null) {
            //Revert the current law's effects
            repealLaw(currentLaw);
        }
        adjustStat(Stat.GOLD, -getLawCost());
        currentLaw = law;
        lawCooldown = 25;
        notifyListeners();
        return true;
    }

    public void repealLaw(Law law) {
        currentLaw = null;
    }

    public int getLawCost() {
        return (int) (playerNation.getValue(Stat.POPULATION) * Law.COST_CONSTANT);
    }

    public void applyLawEffects() {
        for (Stat s : currentLaw.getCounterEffects().keySet()) {
            adjustStat(s, currentLaw.getCounterEffects().get(s));
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

    public void appoint(Role role, Candidate c) {
        appointedRoles.put(role, c);
    }
    public void dismiss(Role role) {
        appointedRoles.remove(role);
    }

    public double getAppointeeBonus(BonusType b) {
        //Also adds Law bonus, if applicable
        double bonus = 1.0;
        for (Role r : appointedRoles.keySet()) {
            if (r.getBonusType() == b) {
                bonus += appointedRoles.get(r).getBonusPercent();
            }
            if (currentLaw != null) {
                for (BonusType e : currentLaw.getBonuses().keySet()) {
                    if (e == b) {
                        bonus += currentLaw.getBonuses().get(e);
                    }
                }
            }
        }
        return 1.0;
    }
    public Candidate getAppointee(Role r) {
        return appointedRoles.getOrDefault(r,null);
    }
    public List<Candidate> getCandidates(Role r) {
        return roleCandidates.get(r);
    }

    public List<Law> getAvailableLaws() {
        return availableLaws;
    }

    public void establishTradeAgreement(Nation n) {
        n.addAgreement(AgreementType.TRADE);
    }

    public boolean buyResource(Nation seller, Stat resource, int qty) {
        if (!seller.hasAgreement(AgreementType.TRADE)) return false;
        double cost = seller.buyPrice(resource) * qty;
        if (playerNation.getValue(Stat.GOLD, 1) < cost) return false;
        playerNation.setValue(Stat.GOLD, playerNation.getValue(Stat.GOLD, 1) - cost);
        adjustStat(resource, qty);
        return true;
    }

    public boolean sellResource(Nation buyer, Stat resource, int qty) {
        if (!buyer.hasAgreement(AgreementType.TRADE)) return false;
        double revenue = buyer.sellPrice(resource) * qty;
        if (playerNation.getValue(resource) < qty) return false;
        playerNation.setValue(Stat.GOLD, playerNation.getValue(Stat.GOLD, 1) + revenue);
        adjustStat(resource, -qty);
        return true;
    }

    private void applyEffect(EventEffect e) {
        if (e.getStat() != Stat.GOLD) {
            adjustStat(e.getStat(), (int) e.getDelta());
        } else {
            adjustStat(e.getStat(),e.getDelta());
        }
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
        double revenue = (pop * taxRate * 0.2);
        revenue *= getAppointeeBonus(BonusType.TAX_INCOME);

        playerNation.setValue(Stat.GOLD, playerNation.getValue(Stat.GOLD) + revenue);

        double threshold = 0.4;
        if (starving) threshold = 0.25;

        if (taxRate > threshold) {
            adjustStat(Stat.STABILITY, -1);
        }
    }

    public double getPowerSupply() {
        double sum = 0;
        if (builtBuildings.isEmpty()) return 0;
        for (String b: builtBuildings.keySet()) {
            assert getBuilding(b) != null;
            sum += (getBuilding(b).getPowerOutput()) * builtBuildings.get(b);
        }
        sum *= getAppointeeBonus(BonusType.POWER_OUTPUT);
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

    public Law getCurrentLaw() {
        return this.currentLaw;
    }

    public int getLawCooldown() {
        return this.lawCooldown;
    }

    public void removePlaceholders(FiredEvent evt) {
        evt.getEvt().setText(evt.getEvt().getText().replace("{source}", evt.getNation().getName()));
        if (evt.getEvt().getEventType() == EventType.TRADE_OFFER) {
            //Pick random resource
            Stat resource = Stat.GOLD;
            Stat[] exclude = {Stat.GOLD, Stat.STABILITY, Stat.POPULATION, Stat.RELATIONS};
            while (Arrays.asList(exclude).contains(resource)) {
                resource = Stat.values()[(int) (Math.random() * Stat.values().length)];
            }

            //Get random resource amount and cost
            int quantity = (int) ((Math.random() * 5) + 1) * 10;
            double delta = evt.getNation().buyPrice(resource) * quantity;

            //Replacing the effects for accepting
            ArrayList<EventEffect> effects = new ArrayList<>();
            effects.add(new EventEffect(Stat.GOLD, -delta));
            effects.add(new EventEffect(resource, quantity));
            evt.getEvt().getOptions().get(0).setResult(new EventResult(effects));

            //Replacing placeholders
            evt.getEvt().setText(evt.getEvt().getText().replace("{resource}", resource.toString()));
            evt.getEvt().setText(evt.getEvt().getText().replace("{quantity}", String.valueOf(quantity)));
            evt.getEvt().setText(evt.getEvt().getText().replace("{cost}", String.format("%.2f", delta)));

        }
    }
}

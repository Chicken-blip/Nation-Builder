package ca.acoii.nationbuilder.nationbuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

public class SimRunner {
    private static final long GOLD_RESERVE = 50;

    public static void main(String[] args) {
        int turns = args.length == 0 ? 500 : Integer.parseInt(args[0]);
        if (turns < 1) {
            System.out.println("turn count must be greater than 1");
            System.exit(1);
        }

        Nation n = new Nation();
        GameEngine engine = new GameEngine(n);
        engine.loadBuildings();
        engine.loadNations();

        Nation xenos = engine.getOtherNations().stream()
                .filter(nation -> nation.getName().equals("Xenos"))
                .findFirst()
                .orElseThrow();

        engine.establishTradeAgreement(xenos);
        if (!engine.sellResource(xenos, Stat.WOOD, 50)
                || !engine.buyResource(xenos, Stat.STONE, 30)) {
            throw new IllegalStateException("Could not afford the opening Weaver investment");
        }
        engine.build("Weaver", 1);



        Path output = Path.of("sim.csv");
        try (BufferedWriter wr = Files.newBufferedWriter(output)) {
            wr.write("turn,gold,population,stability,food,tax rate,farms,weavers,power plants");
            wr.newLine();

            for (int tn = 1; tn < turns; tn++) {
                setTaxPolicy(n,engine);
                boolean builtFarm = tryBuildFarm(n,engine);

                if (tn > 1) {
                    engine.nextTurn();
                }


                wr.write(tn + ","
                        + n.getGold() + ","
                        + n.getValue(Stat.POPULATION) + ","
                        + n.getValue(Stat.STABILITY) + ","
                        + n.getValue(Stat.FOOD) + ","
                        + String.format("%.2f", engine.getTaxRate()) + ","
                        + farmCount(engine) + ","
                        + weaverCount(engine) + ","
                        + powerPlantCount(engine));
                wr.newLine();


            }
            System.out.println("Wrote " + turns + " turns to " + output.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setTaxPolicy(Nation player, GameEngine engine) {
        if (player.getValue(Stat.STABILITY) <= 35) {
            engine.setTaxRate(0.20);
        } else if (engine.isStarving()) {
            engine.setTaxRate(0.25);
        } else {
            engine.setTaxRate(0.40);
        }
    }

    private static boolean tryBuildFarm(Nation player, GameEngine engine) {
        Building farm = engine.getBuildings().stream()
                .filter(building -> building.getName().equals("Farm"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Farm building is missing"));

        int foodProduction = 0;
        for (Building building : engine.getBuildings()) {
            int count = engine.getBuildingCount(building);
            for (EventEffect effect : building.getOutput()) {
                if (effect.getStat() == Stat.FOOD) {
                    foodProduction += effect.getDelta() * count;
                }
            }
        }

        int targetPop = 500000;
        int neededFoodProduction = (int) Math.ceil(targetPop * 0.01);

        if (foodProduction >= neededFoodProduction) {
            return false;
        }

        int sparePower = engine.getPowerSupply() - engine.getPowerDemand();
        if (sparePower < farm.getPowerRequired()
                && !tryBuildPowerPlant(player, engine)) {
            return false;
        }


        for (EventEffect cost : farm.getCost()) {
            if (cost.getDelta() >= 0) {
                continue;
            }

            Stat resource = cost.getStat();
            int missing = -cost.getDelta() - player.getValue(resource);
            if (missing <= 0) {
                continue;
            }

            Nation supplier = engine.getOtherNations().stream()
                    .filter(nation -> nation.buyPrice(resource) > 0)
                    .min(Comparator.comparingDouble(a -> a.buyPrice(resource)))
                    .orElse(null);

            if (supplier == null) {
                return false;
            }

            double purchaseCost = supplier.buyPrice(resource) * missing;
            if (player.getGold() < purchaseCost + GOLD_RESERVE) {
                return false;
            }

            if (!supplier.hasAgreement(AgreementType.TRADE)) {
                engine.establishTradeAgreement(supplier);
            }
            if (!engine.buyResource(supplier, resource, missing)) {
                return false;
            }
        }

        int previousCount = engine.getBuildingCount(farm);
        engine.build(farm.getName(), 1);
        return engine.getBuildingCount(farm) > previousCount;
    }

    private static int farmCount(GameEngine engine) {
        return engine.getBuildings().stream()
                .filter(building -> building.getName().equals("Farm"))
                .mapToInt(engine::getBuildingCount)
                .sum();
    }

    private static int weaverCount(GameEngine engine) {
        return engine.getBuildings().stream()
                .filter(building -> building.getName().equals("Weaver"))
                .mapToInt(engine::getBuildingCount)
                .sum();
    }


    private static int powerPlantCount(GameEngine engine) {
        return engine.getBuildings().stream()
                .filter(building -> building.getName().equals("Power Plant"))
                .mapToInt(engine::getBuildingCount)
                .sum();
    }

    private static boolean tryBuildPowerPlant(Nation player, GameEngine engine) {
        Building plant = engine.getBuildings().stream()
                .filter(building -> building.getName().equals("Power Plant"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Power Plant is missing"));

        if (!buyBuildingMaterials(player, engine, plant)) {
            return false;
        }

        int previousCount = engine.getBuildingCount(plant);
        engine.build(plant.getName(), 1);
        return engine.getBuildingCount(plant) > previousCount;
    }

    private static boolean buyBuildingMaterials(
            Nation player, GameEngine engine, Building building) {
        Map<Stat, Integer> quantities = new EnumMap<>(Stat.class);
        Map<Stat, Nation> suppliers = new EnumMap<>(Stat.class);
        long totalCost = 0;

        for (EventEffect cost : building.getCost()) {
            if (cost.getDelta() >= 0) {
                continue;
            }

            Stat resource = cost.getStat();
            int missing = -cost.getDelta() - player.getValue(resource);
            if (missing <= 0) {
                continue;
            }

            Nation supplier = engine.getOtherNations().stream()
                    .filter(nation -> nation.buyPrice(resource) > 0)
                    .min(Comparator.comparingDouble(a -> a.buyPrice(resource)))
                    .orElse(null);

            if (supplier == null) {
                return false;
            }

            quantities.put(resource, missing);
            suppliers.put(resource, supplier);
            totalCost += supplier.buyPrice(resource) * missing;
        }

        if (player.getGold() < totalCost + GOLD_RESERVE) {
            return false;
        }

        for (Stat resource : quantities.keySet()) {
            Nation supplier = suppliers.get(resource);
            if (!supplier.hasAgreement(AgreementType.TRADE)) {
                engine.establishTradeAgreement(supplier);
            }
            if (!engine.buyResource(supplier, resource, quantities.get(resource))) {
                return false;
            }
        }

        return true;
    }

    private static double weight(Stat stat) {
        return switch (stat) {
            case STABILITY -> 20.0;
            case FOOD -> 2.0;
            default -> 0.25;
        };
    }
}

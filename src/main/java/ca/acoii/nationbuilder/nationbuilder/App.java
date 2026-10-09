package ca.acoii.nationbuilder.nationbuilder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application implements NationListener, GameEngineListener {
    private Label goldLabel, stabLabel, popLabel, powerLabel, turnLabel;
    private StackPane rootStack;
    BorderPane root;
    TilePane menuPane;
    Button turnBtn;
    private Nation yourNation = new Nation();
    private GameEngine engine = new GameEngine(yourNation);
    //Diplomacy Info
    private Label diplomacyHeader, diplomacyDetail, diplomacyAgreements;
    private List<Button> diplomacyBtns = new ArrayList<>();
    private List<Slider> popSliders = new ArrayList<>();
    private List<Label> popTaxLabels = new ArrayList<>();
    private List<Label> popRationLabels = new ArrayList<>();
    private List<Label> popBirthRateLabels = new ArrayList<>();
    private List<Label> popImmigrationLabels = new ArrayList<>();
    private List<Node> popImmigrationNodes = new ArrayList<>();
    private List<Label> infraLabels = new ArrayList<>();
    private Map<Candidate, Button> appointBtns = new HashMap<>();
    private Map<Candidate, Button> dismissBtns = new HashMap<>();
    private Nation selectedNation;

    @Override
    public void start(Stage stage) throws IOException {
        //Main Screen
        root = new BorderPane();

        yourNation.addListener(this);
        engine.addListener(this);
        engine.loadNations();
        engine.loadEvents();
        engine.loadBuildings();
        engine.loadInfrastructure();
        engine.loadAppointees();
        engine.loadLaws();
        engine.loadResearchNodes();

        //Make sliders
        taxSlider();
        rationSlider();
        birthRateSlider();
        immigrationSlider();

        //Resource items
        goldLabel = new Label();
        stabLabel = new Label();
        popLabel = new Label();
        powerLabel = new Label();
        turnLabel = new Label();
        HBox resourcePane = new HBox(10, goldLabel, stabLabel, popLabel, powerLabel, turnLabel);
        resourcePane.setAlignment(Pos.CENTER);
        resourcePane.setPadding(new Insets(10,20,10,20));
        //Menu Items
        int btnWidth = 125;
        int btnHeight = 30;
        Button btn1 = new Button("Construction");
        btn1.setOnAction(e -> {
            showSubMenu("Construction", constructionMenu());
        });
        btn1.setPrefWidth(btnWidth);
        btn1.setPrefHeight(btnHeight);
        Button btn2 = new Button("Trade");
        btn2.setOnAction(e -> {
            showSubMenu("Trade", tradeMenu());
        });
        btn2.setPrefWidth(btnWidth);
        btn2.setPrefHeight(btnHeight);
        Button btn3 = new Button("Diplomacy");
        btn3.setOnAction(e -> {
            showSubMenu("Diplomacy", diplomacyMenu());
        });
        btn3.setPrefWidth(btnWidth);
        btn3.setPrefHeight(btnHeight);
        Button btn4 = new Button("War");
        btn4.setDisable(true);
        btn4.setPrefWidth(btnWidth);
        btn4.setPrefHeight(btnHeight);
        Button btn5 = new Button("Inventory");
        btn5.setOnAction(e -> {
            showSubMenu("Inventory", inventoryMenu());
        });
        btn5.setPrefWidth(btnWidth);
        btn5.setPrefHeight(btnHeight);
        Button btn6 = new Button("Population");
        btn6.setOnAction(e -> {
            showSubMenu("Population", populationMenu());
        });
        btn6.setPrefWidth(btnWidth);
        btn6.setPrefHeight(btnHeight);
        Button btn7 = new Button("Infrastructure");
        btn7.setOnAction(e -> {
            showSubMenu("Infrastructure", infrastructureMenu());
        });
        btn7.setPrefWidth(btnWidth);
        btn7.setPrefHeight(btnHeight);
        Button btn8 = new Button("Appointees");
        btn8.setOnAction(e -> {
            showSubMenu("Appointees", appointeeMenu());
        });
        btn8.setPrefWidth(btnWidth);
        btn8.setPrefHeight(btnHeight);
        Button btn9 = new Button("Laws");
        btn9.setOnAction(e -> {
            showSubMenu("Laws", lawMenu());
        });
        btn9.setPrefWidth(btnWidth);
        btn9.setPrefHeight(btnHeight);
        Button btn10 = new Button("Research");
        btn10.setOnAction(e -> {
            showSubMenu("Research", researchMenu());
        });
        btn10.setPrefWidth(btnWidth);
        btn10.setPrefHeight(btnHeight);
        menuPane = new TilePane(30, 10, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn10);
        menuPane.setPrefColumns(3);
        menuPane.setMaxWidth(500);
        menuPane.setAlignment(Pos.CENTER);
        //Bottom Button
        turnBtn = new Button("NEXT TURN");
        turnBtn.setOnAction(e -> {
            engine.nextTurn();
            FiredEvent evt = engine.fireEvent();
            if (evt != null) {
                FiredEvent updated = engine.removePlaceholders(evt);
                showEventPopup(updated);
            }
        });

        //Formatting Boxes
        HBox bottomPane = new HBox(10, turnBtn);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(10,20,10,20));

        //Root layout
        root.setTop(resourcePane);
        root.setCenter(menuPane);
        root.setBottom(bottomPane);

        rootStack = new StackPane(root);
        stage.setScene(new Scene(rootStack, 800, 550));
        stage.setResizable(false);
        onNationChanged(yourNation);
        onTurnAdvanced(engine);
        stage.show();

        //Testing Area


    }

    private void taxSlider() {
        Slider taxSlider = new Slider(0, 0.80, engine.getTaxRate());
        taxSlider.setShowTickMarks(true);
        taxSlider.setShowTickLabels(true);
        taxSlider.setMajorTickUnit(0.2);
        taxSlider.setBlockIncrement(0.01);
        taxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            engine.setTaxRate(newVal.doubleValue());
            resetPopTaxLabels();
        });
        popSliders.add(taxSlider);
    }
    private void rationSlider() {
        Slider rationSlider = new Slider(0, 3, 0);
        rationSlider.setMinorTickCount(0);
        rationSlider.setMajorTickUnit(1);
        rationSlider.setSnapToTicks(true);
        rationSlider.setShowTickMarks(true);
        rationSlider.setShowTickLabels(true);

        rationSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n < 0.5) return RationSetting.NONE.toString();
                if (n < 1.5) return RationSetting.LOW.toString();
                if (n < 2.5) return RationSetting.MEDIUM.toString();
                return RationSetting.HIGH.toString();
            }

            @Override
            public Double fromString(String s) {
                return switch (RationSetting.valueOf(s)) {
                    case NONE -> 0d;
                    case LOW -> 1d;
                    case MEDIUM -> 2d;
                    default -> 3d;
                };
            }
        });
        rationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            yourNation.setRationSetting(RationSetting.valueOf(rationSlider.getLabelFormatter().toString(newVal.doubleValue())));
            resetPopRationLabels();
        });
        popSliders.add(rationSlider);
    }
    private void birthRateSlider() {
        Slider birthRateSlider = new Slider(0, 2, 0);
        birthRateSlider.setMinorTickCount(0);
        birthRateSlider.setMajorTickUnit(1);
        birthRateSlider.setSnapToTicks(true);
        birthRateSlider.setShowTickMarks(true);
        birthRateSlider.setShowTickLabels(true);

        birthRateSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n < 0.5) return BirthRateSetting.RESTRICTIVE.toString();
                if (n < 1.5) return BirthRateSetting.NEUTRAL.toString();
                return BirthRateSetting.ENCOURAGING.toString();
            }

            @Override
            public Double fromString(String s) {
                return switch (BirthRateSetting.valueOf(s)) {
                    case RESTRICTIVE -> 0d;
                    case NEUTRAL -> 1d;
                    default -> 2d;
                };
            }
        });
        birthRateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            yourNation.setBirthRateSetting(BirthRateSetting.valueOf(birthRateSlider.getLabelFormatter().toString(newVal.doubleValue())));
            resetPopBirthRateLabels();
        });
        popSliders.add(birthRateSlider);
    }
    private void immigrationSlider() {
        Slider immigrationSlider = new Slider(0, 1, yourNation.getImmigrationRate());
        immigrationSlider.setShowTickMarks(true);
        immigrationSlider.setShowTickLabels(true);
        immigrationSlider.setMajorTickUnit(0.2);
        immigrationSlider.setBlockIncrement(0.01);
        immigrationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            yourNation.setImmigrationRate(newVal.doubleValue());
            resetPopImmigrationLabels();
        });
        popSliders.add(immigrationSlider);
    }

    private void showEventPopup(FiredEvent evt) {
        StackPane overlay = new StackPane();
        //Popup overlay - to prevent other interactions
        Region dim = new Region();
        dim.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        //Popup elements
        Label title = new Label("EVENT");
        Text txt = new Text(evt.getEvt().getText());
        HBox btnPanel = new HBox(10);
        btnPanel.setAlignment(Pos.CENTER);

        //Full Popup - Overlay
        VBox popup = new VBox(20, title, txt, btnPanel);
        popup.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: black;");
        popup.setMaxHeight(500);
        popup.setMaxWidth(400);

        //Adding buttons
        for (EventOption evtOpt : evt.getEvt().getOptions()) {
            Button b = new Button(evtOpt.getOption());
            b.setOnAction(e -> {
                applyEffects(evtOpt.getResult());
                rootStack.getChildren().remove(overlay);
            });
            btnPanel.getChildren().add(b);
        }

        //Full overlayed popup
        overlay.getChildren().addAll(dim, popup);
        rootStack.getChildren().add(overlay);
    }

    private void showSubMenu(String title, Node content) {
        Label t = new Label(title);
        t.setMaxWidth(Double.MAX_VALUE);
        t.setAlignment(Pos.CENTER);

        VBox subMenu = new VBox(10, t, content);
        Button btn = new Button("Back");
        btn.setOnAction(e -> {
            root.setCenter(menuPane);
            turnBtn.setDisable(false);
        });
        HBox btnBox = new HBox(btn);
        btnBox.setAlignment(Pos.CENTER);
        subMenu.getChildren().add(btnBox);
        subMenu.setFillWidth(true);
        ScrollPane scrl = new ScrollPane(subMenu);
        scrl.setFitToWidth(true);
        root.setCenter(scrl);
        turnBtn.setDisable(true);
    }

    private VBox inventoryMenu() {
        VBox main = new VBox(10);
        main.setPadding(new Insets(50));

        List<Stat> stats = new ArrayList<>(List.of(new Stat[]{Stat.GOLD, Stat.RELATIONS, Stat.STABILITY}));
        Label n;
        for (Stat s : Stat.values()) {
            if (!stats.contains(s)) {
                n = new Label(s.toString() + ": " + yourNation.getValue(s));
                main.getChildren().add(n);
            }
        }

        return main;
    }
    private GridPane constructionMenu() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Name"), 0, 0);
        grid.add(new Label("Building cost"), 1, 0);
        grid.add(new Label("Output per turn"), 2, 0);
        grid.add(new Label("Upkeep"), 3, 0);
        grid.add(new Label("Current Count"), 4, 0);

        int row = 1;
        for (Building b : engine.getBuildings()) {
            Label name = new Label(b.getName());
            TitledPane cost = new TitledPane();
            cost.setText("Expand");
            cost.setContent(new VBox(10, new Label(formatEffects(b.getCost(), 1, b))));
            cost.setExpanded(false);
            Label output = new Label(formatEffects(b.getOutput(), 0, b));
            Label upkeep = new Label(b.getUpkeep() + " gold/turn");
            Label count = new Label("Owned: " + engine.getBuildingCount(b));
            Button buildBtn = new Button("Build");
            buildBtn.setOnAction(e -> {
                if (engine.build(b.getName(), 1)) {
                    System.out.println("Successfully build " + b.getName());
                    powerLabel.setText("Power: " + engine.getPowerDemand() + " / " + engine.getPowerSupply());
                } else {
                    System.out.println("Failed to build " + b.getName());
                }
                showSubMenu("Construction", constructionMenu());
            });
            grid.add(name, 0, row);
            grid.add(cost, 1, row);
            grid.add(output, 2, row);
            grid.add(upkeep, 3, row);
            grid.add(count, 4, row);
            grid.add(buildBtn, 5, row);

            row++;
        }

        return grid;
    }

    private String formatEffects(List<EventEffect> cost, int type, Object source) {
        String str = "";
        double bonus = 1.0;
        if (source instanceof Building) {
            if (((Building) source).getBuildingSector() == BuildingSector.INDUSTRY) {
                bonus = engine.getBonusMultiplier(BonusType.INDUSTRY_OUTPUT);
            } else if (((Building) source).getBuildingSector() == BuildingSector.AGRICULTURE) {
                bonus = engine.getBonusMultiplier(BonusType.AGRICULTURE_OUTPUT);
            }
        }
        switch (type) {
            case 0 -> {
                //Default, longlist
                for (EventEffect e : cost) {
                    if (bonus != 1.0 && e.getDelta() > 0) {
                        str += e.getStat() + ": " + String.format("%.0f", (e.getDelta() * bonus));
                    } else {
                        str += e.getStat() + ": " + String.format("%.0f", e.getDelta());
                    }
                    if (e != cost.getLast()) {
                        str += ", ";
                    }
                }
            }
            case 1 -> {
                //Actual list ver
                for (EventEffect e : cost) {
                    str += e.getStat() + ": " + e.getDelta();
                    if (e != cost.getLast()) {
                        str += "\n";
                    }
                }
            }
        }
        return str;
    }

    private HBox diplomacyMenu() {
        HBox main = new HBox(20);
        main.setPadding(new Insets(20));
        diplomacyBtns = new ArrayList<>();

        //Left Side
        ListView nationList = new ListView<>();
        nationList.setPrefHeight(150);
        nationList.setCellFactory(listView -> new ListCell<Nation>() {
            @Override
            protected void updateItem(Nation nation, boolean empty) {
                super.updateItem(nation, empty);
                setText(empty || nation == null ? null : nation.getName());
            }
        });
        nationList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, n) -> {
                    if (n != null) {
                        updateDiplomacyDetail((Nation) n);
                    }
                });
        nationList.getItems().setAll(engine.getOtherNations());
        main.getChildren().add(nationList);

        //Right side
        VBox nationDetails = new VBox(10);
        diplomacyHeader = new Label("Nation - ");
        diplomacyDetail = new Label("Select a nation");
        diplomacyAgreements = new Label("");
        nationDetails.getChildren().addAll(diplomacyHeader, diplomacyDetail, diplomacyAgreements);
        Button btnSendGift = new Button("Send Gift (-50 Gold)");
        btnSendGift.setOnAction(e -> {
            System.out.println("Clicked. Gold=" + yourNation.getValue(Stat.GOLD) +
                    " Relations=" + selectedNation.getValue(Stat.RELATIONS));
            if (selectedNation == null || yourNation.getValue(Stat.GOLD) < 50) return;
            yourNation.setValue(Stat.GOLD, yourNation.getValue(Stat.GOLD) - 50);
            selectedNation.setValue(Stat.GOLD, selectedNation.getValue(Stat.GOLD) + 50);
            selectedNation.setValue(Stat.RELATIONS, selectedNation.getValue(Stat.RELATIONS) + 15);
            updateDiplomacyDetail(selectedNation);
        });
        btnSendGift.setDisable(true);
        diplomacyBtns.add(btnSendGift);
        Button btnTradeAgreement = new Button("Send Trade Agreement");
        btnTradeAgreement.setOnAction(e -> {
            engine.establishTradeAgreement(selectedNation);
            updateDiplomacyDetail(selectedNation);
        });
        btnTradeAgreement.setDisable(true);
        diplomacyBtns.add(btnTradeAgreement);
        nationDetails.getChildren().addAll(btnSendGift, btnTradeAgreement);
        main.getChildren().add(nationDetails);

        return main;
    }

    private void updateDiplomacyDetail(Nation n) {
        selectedNation = n;
        diplomacyHeader.setText("Nation - " + n.getName());
        diplomacyDetail.setText("Current Relation: " + n.getValue(Stat.RELATIONS));
        if (selectedNation.getAgreements().isEmpty()) {
            diplomacyAgreements.setText("Agreements: N/A");
        } else {
            String str = "Agreements: ";
            for (AgreementType a : selectedNation.getAgreements()) {
                str += a.toString();
                if (!(selectedNation.getAgreements().getLast() == a)) {
                    str += ", ";
                }
            }
            diplomacyAgreements.setText(str);
        }
        diplomacyBtns.getFirst().setDisable(yourNation.getValue(Stat.GOLD) < 50);
        diplomacyBtns.get(1).setDisable(selectedNation.hasAgreement(AgreementType.TRADE));
    }

    private HBox tradeMenu() {
        HBox main = new HBox(20);
        main.setPadding(new Insets(20));

        //Left Side - list of resources + disclaimer label
        VBox left = new VBox(30);
        left.setPadding(new Insets(20));

        ListView<Stat> resources = new ListView<>();
        resources.setPrefHeight(150);
        List<Stat> excludes = List.of(new Stat[]{Stat.GOLD, Stat.RELATIONS, Stat.STABILITY, Stat.POPULATION});
        for (Stat s : Stat.values()) {
            if (!excludes.contains(s)) {
                resources.getItems().add(s);
            }
        }
        resources.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, s) -> {
                    if (s != null) {
                        //Update right-side of pane
                        if (main.getChildren().size() > 1) {
                            main.getChildren().removeLast();
                        }
                        main.getChildren().add(tradeSideMenu(s));

                    }
                });

        Label disclaimer = new Label("NOTE: All trades are subject to a " + (100 * engine.getTradeLogisticExpense()) + "% logistics expense");

        left.getChildren().addAll(resources, disclaimer);

        main.getChildren().add(left);
        return main;
    }

    private VBox tradeSideMenu(Stat s) {
        //Right Side - VBox with quantity input, details of other nations, and buy/sell btns
        VBox side = new VBox(20);
        side.setPadding(new Insets(20));

        //Field for quantity input
        TextField qtyInput = new TextField("Enter an amount here");

        //Grid for all the nation details and buttons
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        //Header row here
        int row = 1;
        for (Nation n : engine.getOtherNations()) {
            Label name = new Label(n.getName());
            Label sellP = new Label(n.sellPrice(s) + " gold");
            Label buyP = new Label(n.buyPrice(s) + " gold");
            Button sellBtn = new Button("Sell");
            Button buyBtn = new Button("Buy");

            buyBtn.setDisable(!n.hasAgreement(AgreementType.TRADE));
            sellBtn.setDisable(!n.hasAgreement(AgreementType.TRADE));

            buyBtn.setOnAction(e -> {
                int qty = parseInt(qtyInput);
                if (qty < 0) {
                    System.out.println("Invalid input!");
                    return;
                }
                if (engine.buyResource(n,s,qty)) {
                    System.out.println("Successfully bought " + qty + " " + s.toString());
                } else {
                    System.out.println("Unable to purchase " + qty + " " + s.toString());
                }
            });
            sellBtn.setOnAction(e -> {
                int qty = parseInt(qtyInput);
                if (qty < 0) {
                    System.out.println("Invalid input!");
                    return;
                }
                if (engine.sellResource(n,s,qty)) {
                    System.out.println("Successfully sold " + qty + " " + s.toString());
                } else {
                    System.out.println("Unable to sell " + qty + " " + s.toString());
                }
            });

            grid.add(name, 0, row);
            grid.add(sellP, 1, row);
            grid.add(buyP, 2, row);
            grid.add(sellBtn, 3, row);
            grid.add(buyBtn, 4, row);

            row++;
        }
        side.getChildren().addAll(qtyInput, grid);
        return side;
    }

    private HBox populationMenu() {
        HBox main = new HBox(10);
        main.setPadding(new Insets(20));

        //Population Submenus
        VBox btnContainer = new VBox(30);
        btnContainer.setPadding(new Insets(20));
        //Buttons
        Button taxBtn = new Button("Taxes");
        taxBtn.setOnAction(e -> {
            if (main.getChildren().size() > 1) {
                main.getChildren().removeLast();
            }
            main.getChildren().add(getPopTaxPane());
        });
        Button rationBtn = new Button("Rationing");
        rationBtn.setOnAction(e -> {
            if (main.getChildren().size() > 1) {
                main.getChildren().removeLast();
            }
            main.getChildren().add(getPopRationPane());
        });
        Button birthRateBtn = new Button("Birth Rate Policy");
        birthRateBtn.setOnAction(e -> {
            if (main.getChildren().size() > 1) {
                main.getChildren().removeLast();
            }
            main.getChildren().add(getPopBirthRatePane());
        });
        Button immigrationBtn = new Button("Immigration Stance");
        immigrationBtn.setOnAction(e -> {
            if (main.getChildren().size() > 1) {
                main.getChildren().removeLast();
            }
            main.getChildren().add(getPopImmigrationPane());
        });

        btnContainer.getChildren().addAll(taxBtn, rationBtn, birthRateBtn, immigrationBtn);

        main.getChildren().add(btnContainer);

        return main;
    }

    private VBox getPopTaxPane() {
        popTaxLabels = new ArrayList<>();
        //Tax Pane
        VBox taxPane = new VBox(10);
        taxPane.setPadding(new Insets(20));

        Label title = new Label("Tax Management");
        Label description = new Label("Set the percent of tax you get from your population.");

        //Labels below
        Label taxLabel1 = new Label("Tax Rate: " + String.format("%.2f", (engine.getTaxRate() * 100)) + "%");
        Label taxLabel2 = new Label("Projected income: " + String.format("%.2f", (engine.getTaxRate() * yourNation.getValue(Stat.POPULATION) * 0.5 * engine.getBonusMultiplier(BonusType.TAX_INCOME))) + "/turn");
        Label taxLabel3 = new Label("Unrest Threshold: " + (engine.getTaxThreshold() * 100));
        Label taxLabel4 = new Label("");
        if (engine.getTaxRate() > engine.getTaxThreshold()) {
            taxLabel4 = new Label("Stability penalty: " + engine.getTaxPenalty() + "/turn");
        } else {
            taxLabel4 = new Label("No stability penalty");
        }
        popTaxLabels.addAll(List.of(new Label[]{taxLabel1, taxLabel2, taxLabel3, taxLabel4}));

        taxPane.getChildren().addAll(title, description, popSliders.get(0), taxLabel1, taxLabel2, taxLabel3, taxLabel4);
        return taxPane;
    }
    private void resetPopTaxLabels() {
        popTaxLabels.get(0).setText("Tax Rate: " + String.format("%.2f", (engine.getTaxRate() * 100)) + "%");
        popTaxLabels.get(1).setText("Projected income: " + String.format("%.2f", (engine.getTaxRate() * yourNation.getValue(Stat.POPULATION) * 0.5)) + "/turn");
        popTaxLabels.get(2).setText("Unrest Threshold: " + (engine.getTaxThreshold() * 100));
        if (engine.getTaxRate() > engine.getTaxThreshold()) {
            popTaxLabels.get(3).setText("Stability penalty: " + engine.getTaxPenalty() + "/turn");
        } else {
            popTaxLabels.get(3).setText("No stability penalty");
        }
    }

    private VBox getPopRationPane() {
        popRationLabels = new ArrayList<>();
        VBox rationPane = new VBox(10);
        rationPane.setPadding(new Insets(20));

        Label title = new Label("Rationing");
        Label description = new Label("Reduce the food consumption per turn.");

        Label name = new Label("Currently set to: " + yourNation.getRationSetting().toString());
        Label reduction = new Label("Food Reduction: " + (engine.getRationReduction() * 100) + "%");
        Label stab = new Label("Stability Penalty: " + engine.getRationPenalty() + "/turn");

        popRationLabels.addAll(List.of(new Label[]{name, reduction, stab}));

        rationPane.getChildren().addAll(title, description, popSliders.get(1), name, reduction, stab);

        return rationPane;
    }

    private void resetPopRationLabels() {
        popRationLabels.getFirst().setText("Currently set to: " + yourNation.getRationSetting().toString());
        popRationLabels.get(1).setText("Food Reduction: " + (engine.getRationReduction() * 100) + "%");
        popRationLabels.get(2).setText("Stability Penalty: " + engine.getRationPenalty() + "/turn");
    }

    private VBox getPopBirthRatePane() {
        popBirthRateLabels = new ArrayList<>();
        VBox birthRatePane = new VBox(10);
        birthRatePane.setPadding(new Insets(20));

        Label title = new Label("Birth Rate Policy");
        Label description = new Label("Control the rate at which the population grows.");

        Label name = new Label("Currently set to: " + yourNation.getBirthRateSetting().toString());
        Label growth = new Label("Growth Multiplier: " + (engine.getBirthRatePolicy() * 100) + "%");
        Label cost = new Label("Upkeep: " + engine.getBirthRatePolicyUpkeep() + " gold/turn");

        popBirthRateLabels.addAll(List.of(new Label[]{name, growth, cost}));

        birthRatePane.getChildren().addAll(title, description, popSliders.get(2), name, growth, cost);

        return birthRatePane;
    }

    private void resetPopBirthRateLabels() {
        popBirthRateLabels.getFirst().setText("Currently set to: " + yourNation.getBirthRateSetting().toString());
        popBirthRateLabels.get(1).setText("Growth Multiplier: " + (engine.getBirthRatePolicy() * 100) + "%");
        popBirthRateLabels.get(2).setText("Upkeep: " + engine.getBirthRatePolicyUpkeep() + "/turn");
    }

    private VBox getPopImmigrationPane() {
        popImmigrationLabels = new ArrayList<>();
        VBox immigrationPane = new VBox(10);
        immigrationPane.setPadding(new Insets(20));

        Label title = new Label("Immigration");
        Label description = new Label("Control the intake of immigrants to your nation");
        Label disclaimer = new Label("NOTE: Projected immigration is dependent on your current nation's prosperity as well as the set acceptance rate");
        disclaimer.setWrapText(true);

        Button toggle = new Button("");
        toggle.setText(yourNation.isImmigrationOpen() ? "CLOSE BORDERS" : "OPEN BORDERS");
        popSliders.get(3).setDisable(!yourNation.isImmigrationOpen());
        toggle.setOnAction(e -> {
            yourNation.toggleImmigration();
            toggle.setText(yourNation.isImmigrationOpen() ? "CLOSE BORDERS" : "OPEN BORDERS");
            popSliders.get(3).setDisable(!yourNation.isImmigrationOpen());
            if (!yourNation.isImmigrationOpen()) {
                yourNation.setImmigrationRate(0);
                popSliders.get(3).setValue(0);
            }
            resetPopImmigrationLabels();
        });

        Label name = new Label("Immigration status: " + (yourNation.isImmigrationOpen() ? "OPEN" : "CLOSED"));
        Label acceptRate = new Label("Acceptance rate: " + String.format("%.2f", (yourNation.getImmigrationRate() * 100)) + "%");
        Label prospect = new Label("Projected immigration: " + engine.projectedImmigration() + "/turn");

        popImmigrationLabels.addAll(List.of(new Label[]{name, acceptRate, prospect}));

        immigrationPane.getChildren().addAll(title, description, disclaimer, toggle, popSliders.get(3), name, acceptRate, prospect);

        return immigrationPane;
    }

    public void resetPopImmigrationLabels() {
        popImmigrationLabels.get(0).setText("Immigration status: " + (yourNation.isImmigrationOpen() ? "OPEN" : "CLOSED"));
        popImmigrationLabels.get(1).setText("Acceptance rate: " + String.format("%.2f", (yourNation.getImmigrationRate() * 100)) + "%");
        popImmigrationLabels.get(2).setText("Projected immigration: " + engine.projectedImmigration() + "/turn");
    }

    private HBox infrastructureMenu() {
        HBox main = new HBox(10);
        main.setPadding(new Insets(20));

        //Left side - list of infrastructure categories
        ListView<InfraCategory> categories = new ListView<>();
        categories.setPrefHeight(150);
        for (InfraCategory i : InfraCategory.values()) {
            categories.getItems().add(i);
        }
        categories.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, s) -> {
                    if (s != null) {
                        //Update right-side of pane
                        if (main.getChildren().size() > 1) {
                            main.getChildren().removeLast();
                        }
                        main.getChildren().add(infrastructureSideMenu(s));

                    }
                });
        categories.setMaxWidth(150);
        categories.setMinWidth(150);

        main.getChildren().add(categories);

        return main;
    }

    private VBox infrastructureSideMenu(InfraCategory s) {
        VBox side = new VBox(20);
        side.setPadding(new Insets(20));

        //Heading
        Label heading = new Label("Infrastructure - " + s.toString());

        //Info label for current status of happiness
        Label current = new Label("Provided Happiness: " + engine.getInfraHappiness(s) + " | Required Happiness: " + engine.getHappinessRequired(s));

        //Grid for each infrastructure option under the category
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name"),0,0);
        grid.add(new Label("Cost"), 1, 0);
        grid.add(new Label("Energy Cost"), 2, 0);
        grid.add(new Label("Happiness"), 3, 0);
        grid.add(new Label("Upkeep"), 4, 0);
        grid.add(new Label("Current #"), 5, 0);

        int row = 1;

        //TODO: Add actual infrastructure rows
        for (Infrastructure i : engine.getInfrastructure()) {
            if (i.getCategory() == s) {
                Label name = new Label(i.getName());
                TitledPane cost = new TitledPane();
                cost.setText("Expand");
                cost.setContent(new VBox(10, new Label(formatEffects(i.getCost(), 1, i))));
                cost.setExpanded(false);
                Label energy = new Label(i.getPowerRequired() + "");
                Label happiness = new Label(i.getHappiness() + "");
                Label upkeep = new Label(i.getUpkeep() + " gold/turn");
                Label count = new Label(engine.getInfrastructureCount(i) + "");
                Button buildBtn = new Button("Build");
                buildBtn.setOnAction(e -> {
                    if (engine.build(i.getName(), 2)) {
                        System.out.println("Successfully build " + i.getName());
                    } else {
                        System.out.println("Failed to build " + i.getName());
                    }
                    count.setText(engine.getInfrastructureCount(i) + "");
                    current.setText("Provided Happiness: " + engine.getInfraHappiness(s) + " | Required Happiness: " + engine.getHappinessRequired(s));
                    powerLabel.setText("Power: " + engine.getPowerDemand() + " / " + engine.getPowerSupply());
                });

                grid.add(name, 0, row);
                grid.add(cost, 1, row);
                grid.add(energy, 2, row);
                grid.add(happiness, 3, row);
                grid.add(upkeep, 4, row);
                grid.add(count, 5, row);
                grid.add(buildBtn, 6, row);

                row++;

            }
        }

        //grid.setMaxWidth(400);

        side.getChildren().addAll(heading, current, grid);
        return side;
    }

    private HBox appointeeMenu() {
        HBox main = new HBox(50);
        main.setPadding(new Insets(20));

        ListView<Role> categories = new ListView<>();
        categories.setPrefHeight(150);
        for (Role r : Role.values()) {
            categories.getItems().add(r);
        }
        categories.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, s) -> {
                    if (s != null) {
                        //Update right-side of pane
                        if (main.getChildren().size() > 1) {
                            main.getChildren().removeLast();
                        }
                        appointBtns = new HashMap<>();
                        main.getChildren().add(appointeeSideMenu(s));

                    }
                });

        categories.setMaxWidth(150);
        categories.setMinWidth(150);

        main.getChildren().add(categories);

        return main;
    }

    private VBox appointeeSideMenu(Role s) {
        VBox side = new VBox(20);
        side.setPadding(new Insets(20));

        Label heading = new Label("Current Appointee: " + (engine.getAppointee(s) != null ? engine.getAppointee(s).getName() : "N/A"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name"), 0, 0);
        grid.add(new Label("Bonus"), 1, 0);
        grid.add(new Label("Upkeep"), 2, 0);

        int row = 1;

        for (Candidate c : engine.getCandidates(s)) {
            Label name = new Label(c.getName());
            Label bonus = new Label(String.format("%.2f", c.getBonusPercent() * 100.0));
            Label upkeep = new Label(c.getUpkeep() + " gold/turn");
            Button appBtn = new Button("Appoint");
            Button disBtn = new Button("Dismiss");
            if (engine.getAppointee(s) == c) appBtn.setDisable(true);
            if (engine.getAppointee(s) != c) disBtn.setDisable(true);
            appBtn.setOnAction(e -> {
                Candidate prior = engine.getAppointee(s);
                engine.appoint(s, c);
                refreshAppointeeScreen(s, prior, c);
            });
            disBtn.setOnAction(e -> {
                engine.dismiss(s);
                refreshAppointeeScreen(s, c, null);
            });

            appointBtns.put(c, appBtn);
            dismissBtns.put(c, disBtn);

            grid.add(name, 0, row);
            grid.add(bonus, 1, row);
            grid.add(upkeep, 2, row);
            grid.add(appBtn, 3, row);
            grid.add(disBtn, 4, row);

            row++;
        }

        side.getChildren().addAll(heading, grid);

        return side;
    }

    private void refreshAppointeeScreen(Role s, Candidate prior, Candidate c) {
        if (appointBtns.get(prior) != null) {
            appointBtns.get(prior).setDisable(false);
            dismissBtns.get(prior).setDisable(true);
        }
        if (appointBtns.get(c) != null) {
            appointBtns.get(c).setDisable(true);
            dismissBtns.get(c).setDisable(false);
        }
    }

    private VBox lawMenu() {
        VBox main = new VBox(30);
        main.setPadding(new Insets(30));

        Label lawLabel = new Label("Current Law: " + (engine.getCurrentLaw() == null ? "N/A" : engine.getCurrentLaw().getName()));
        Label cooldownLabel = new Label("Remaining Cooldown Turns: " + (engine.getLawCooldown() == 0 ? "N/A" : engine.getLawCooldown()));
        Label costLabel = new Label("Law Cost: " + engine.getLawCost() + " gold");

        main.getChildren().addAll(lawLabel, cooldownLabel, costLabel);

        VBox submain = new VBox(5);
        submain.setPadding(new Insets(10));

        for (Law l : engine.getAvailableLaws()) {
            HBox container = new HBox(10);
            container.setPadding(new Insets(10));
            container.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(l.getName());
            name.setMinWidth(150);

            Label desc = new Label(l.getDescription());
            desc.setPrefWidth(300);
            desc.setWrapText(true);
            HBox.setHgrow(desc, Priority.ALWAYS);
            Button enactBtn = new Button("Enact");
            if (engine.getCurrentLaw() == l) {
                enactBtn.setDisable(true);
            }
            enactBtn.setOnAction(e -> {
                if (engine.enactLaw(l)) {
                    System.out.println("Successfully enacted " + l.getName());
                } else {
                    System.out.println("Failed to enact " + l.getName());
                }
                showSubMenu("Laws", lawMenu());
            });

            container.getChildren().addAll(name, desc, enactBtn);

            submain.getChildren().add(container);
        }

        main.getChildren().add(submain);

        return main;
    }

    private HBox researchMenu() {
        HBox main = new HBox(10);
        main.setPadding(new Insets(20));

        TreeView<ResearchNode> tree = new TreeView<>();
        tree.setPadding(new Insets(20));
        tree.setPrefHeight(150);
        tree.setCellFactory(tv -> new TreeCell<ResearchNode>() {
            @Override
            protected void updateItem(ResearchNode item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item.getName());
                } else {
                    setText(null);
                }
            }
        });

        //First run, make treeitems of each node
        Map<ResearchNode, TreeItem<ResearchNode>> list = new HashMap<>();
        for (ResearchNode n : engine.getResearchNodes()) {
            TreeItem<ResearchNode> node = new TreeItem<>(n);
            list.put(n, node);

        }
        //Second run, attach children to parents
        for (ResearchNode n : list.keySet()) {
            if (!n.getPrerequisites().isEmpty()) {
                for (String x : n.getPrerequisites()) {
                    list.get(engine.getResearchNode(x)).getChildren().add(list.get(n));
                }
            }
        }

        tree.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() != null) {
                if (main.getChildren().size() > 1) {
                    main.getChildren().removeLast();
                }
                main.getChildren().add(showResearchSubMenu(newItem.getValue()));
            }
        });

        //Third run, add all root parents to the root
        TreeItem<ResearchNode> root = new TreeItem<>();
        for (ResearchNode n : list.keySet()) {
            if (n.getPrerequisites().isEmpty()) {
                root.getChildren().add(list.get(n));
            }
        }
        tree.setRoot(root);
        tree.setShowRoot(false);

        //Final run, expand every root to show their children
        for (ResearchNode n : list.keySet()) {
            if (n.getPrerequisites().isEmpty()) {
                list.get(n).setExpanded(true);
            }
        }
        main.getChildren().add(tree);

        return main;
    }

    private VBox showResearchSubMenu(ResearchNode value) {
        VBox side = new VBox(10);
        side.setPadding(new Insets(20));

        Label name = new Label(value.getName());
        TitledPane bonuses = new TitledPane();
        bonuses.setText("Bonuses");
        bonuses.setContent(new VBox(10, new Label(formatBonusTypeMap(value))));
        bonuses.setExpanded(false);
        Label researchTime = new Label("Research Time: " + value.getResearchTime());
        Label startCost = new Label("Research Start Cost: " + value.getStartCost() + " GOLD");
        Label maintCost = new Label("Research Maintenance Cost: " + value.getMaintenanceCost() + " gold/turn");
        Button research = new Button("Research");
        research.setOnAction(e -> {
            if (engine.beginResearch(value)) {
                research.setDisable(true);
            };
        });
        if (engine.isResearched(value) || engine.getResearchTime() > 0) {
            research.setDisable(true);
        }

        side.getChildren().addAll(name, bonuses, researchTime, startCost, maintCost, research);
        return side;
    }

    private String formatBonusTypeMap(ResearchNode value) {
        //Create new format for Map BonusType effects
        String str = "";
        for (BonusType b : value.getBonuses().keySet()) {
            switch (b) {
                case AGRICULTURE_OUTPUT -> {
                    str += "Boost agricultural building output by " + String.format("%d%%", Math.round(value.getBonuses().get(b) * 100)) + "\n";
                }
                case BonusType.INDUSTRY_OUTPUT -> {
                    str += "Boost industrial building output by " + String.format("%d%%", Math.round(value.getBonuses().get(b) * 100)) + "\n";
                }
                case BonusType.POPULATION_GROWTH -> {
                    str += "Boost population growth by " +String.format("%d%%", Math.round(value.getBonuses().get(b) * 100)) + "\n";
                }
                case BonusType.POWER_OUTPUT -> {
                    str += "Boost power supply by " +String.format("%d%%", Math.round(value.getBonuses().get(b) * 100)) + "\n";
                }
                case BonusType.TAX_INCOME -> {
                    str += "Boost tax income by " +String.format("%d%%", Math.round(value.getBonuses().get(b) * 100)) + "\n";}
            }
        }
        return str.trim();
    }

    private int parseInt(TextField field) {
        try {
            String str = field.getText();
            int value = Integer.parseInt(str.trim());
            if (value < 0) {
                value = -1;
            }
            return value;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void onNationChanged(Nation nation) {
        goldLabel.setText("Gold: " + String.format("%.2f", nation.getValue(Stat.GOLD, 1)));
        stabLabel.setText("Stability: " + nation.getValue(Stat.STABILITY));
        popLabel.setText("Population: " + nation.getValue(Stat.POPULATION));
        if (engine.getPowerSupply() < engine.getPowerDemand()) {
            powerLabel.setTextFill(Color.RED);
            powerLabel.setText("Power: " + engine.getPowerDemand() + " / " + ((int) engine.getPowerSupply()) + " | HALTED");
        } else {
            powerLabel.setText("Power: " + engine.getPowerDemand() + " / " + ((int) engine.getPowerSupply()));
            powerLabel.setTextFill(Color.BLACK);
        }
    }

    @Override
    public void onTurnAdvanced(GameEngine game) {
        turnLabel.setText("Turn: " + game.getTurn());


    }

    private void applyEffects(EventResult evtResult) {
        for (EventEffect e : evtResult.getEffects()) {
            double current = 0;
            double change = e.getDelta();
            current = yourNation.getValue(e.getStat());
            if (e.getStat() == Stat.GOLD) {
                yourNation.setValue(Stat.GOLD, yourNation.getValue(Stat.GOLD) + change);
            } else {
                yourNation.setValue(e.getStat(), (int) (current + change));
            }
        }
    }
}

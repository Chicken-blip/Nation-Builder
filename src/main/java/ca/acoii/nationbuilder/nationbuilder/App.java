package ca.acoii.nationbuilder.nationbuilder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private List<Label> popLabels = new ArrayList<>();
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
        int btnWidth = 100;
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
        menuPane = new TilePane(30, 10, btn1, btn2, btn3, btn4, btn5, btn6);
        menuPane.setPrefColumns(4);
        menuPane.setAlignment(Pos.CENTER);
        //Bottom Button
        turnBtn = new Button("NEXT TURN");
        turnBtn.setOnAction(e -> {
            engine.nextTurn();
            FiredEvent evt = engine.fireEvent();
            if (evt != null) { showEventPopup(evt); }
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
        stage.setScene(new Scene(rootStack, 650, 400));
        onNationChanged(yourNation);
        onTurnAdvanced(engine);
        stage.show();

        //Testing Area


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
        grid.add(new Label("# of Buildings"), 4, 0);

        int row = 1;
        for (Building b : engine.getBuildings()) {
            Label name = new Label(b.getName());
            TitledPane cost = new TitledPane();
            cost.setText("Expand");
            cost.setContent(new VBox(10, new Label(formatEffects(b.getCost(), 1))));
            cost.setExpanded(false);
            Label output = new Label(formatEffects(b.getOutput(), 0));
            Label upkeep = new Label(b.getUpkeep() + " gold/turn");
            Label count = new Label("Owned: " + engine.getBuildingCount(b));
            Button buildBtn = new Button("Build");
            buildBtn.setOnAction(e -> {
                engine.build(b.getName());
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

    private String formatEffects(List<EventEffect> cost, int type) {
        String str = "";
        switch (type) {
            case 0 -> {
                //Default, longlist
                for (EventEffect e : cost) {
                    str += e.getStat() + ": " + e.getDelta();
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

        //Left Side - list of resources
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

        main.getChildren().add(resources);

        return main;
    }

    private VBox tradeSideMenu(Stat s) {
        //Right Side - VBox with quanitty input, details of other nations, and buy/sell btns
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

    private VBox populationMenu() {
        popLabels = new ArrayList<>();
        VBox main = new VBox(10);
        main.setPadding(new Insets(20));

        //Tax Pane
        Slider taxSlider = new Slider(0, 0.80, engine.getTaxRate());
        taxSlider.setShowTickMarks(true);
        taxSlider.setShowTickLabels(true);
        taxSlider.setMajorTickUnit(0.2);
        taxSlider.setBlockIncrement(0.01);
        taxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            engine.setTaxRate(newVal.doubleValue());
            resetPopLabels();
        });

        //Labels below
        Label taxLabel1 = new Label("Tax Rate: " + String.format("%.2f", (engine.getTaxRate() * 100)) + "%");
        Label taxLabel2 = new Label("Projected income: " + String.format("%.2f", (engine.getTaxRate() * yourNation.getValue(Stat.POPULATION) * 0.5)) + "/turn");
        Label taxLabel3 = new Label("");
        Label taxLabel4 = new Label("");
        if (engine.isStarving()) {
            taxLabel3 = new Label("Unrest Threshold: 25%");
            if (engine.getTaxRate() > 0.25) {
                taxLabel4 = new Label("Stability penalty: -1/turn");
            } else {
                taxLabel4 = new Label("No stability penalty");
            }
        } else {
            taxLabel3 = new Label("Unrest Threshold: 40%");
            if (engine.getTaxRate() > 0.4) {
                taxLabel4 = new Label("Stability penalty: -1/turn");
            } else {
                taxLabel4 = new Label("No stability penalty");
            }
        }
        popLabels.addAll(List.of(new Label[]{taxLabel1, taxLabel2, taxLabel3, taxLabel4}));

        main.getChildren().add(taxSlider);
        main.getChildren().addAll(taxLabel1, taxLabel2, taxLabel3, taxLabel4);
        return main;
    }

    private void resetPopLabels() {
        popLabels.get(0).setText("Tax Rate: " + String.format("%.2f", (engine.getTaxRate() * 100)) + "%");
        popLabels.get(1).setText("Projected income: " + String.format("%.2f", (engine.getTaxRate() * yourNation.getValue(Stat.POPULATION) * 0.5)) + "/turn");
        if (engine.isStarving()) {
            popLabels.get(2).setText("Unrest Threshold: 25%");
            if (engine.getTaxRate() > 0.25) {
                popLabels.get(3).setText("Stability penalty: -1/turn");
            } else {
                popLabels.get(3).setText("No stability penalty");
            }
        } else {
            popLabels.get(2).setText("Unrest Threshold: 40%");
            if (engine.getTaxRate() > 0.4) {
                popLabels.get(3).setText("Stability penalty: -1/turn");
            } else {
                popLabels.get(3).setText("No stability penalty");
            }
        }
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
        goldLabel.setText("Gold: " + nation.getValue(Stat.GOLD));
        stabLabel.setText("Stability: " + nation.getValue(Stat.STABILITY));
        popLabel.setText("Population: " + nation.getValue(Stat.POPULATION));
        if (engine.getPowerSupply() < engine.getPowerDemand()) {
            powerLabel.setText("Power: " + engine.getPowerDemand() + " / " + engine.getPowerSupply() + " | HALTED");
            powerLabel.setTextFill(Color.RED);
        } else {
            powerLabel.setText("Power: " + engine.getPowerDemand() + " / " + engine.getPowerSupply());
            powerLabel.setTextFill(Color.BLACK);
        }
    }

    @Override
    public void onTurnAdvanced(GameEngine game) {
        turnLabel.setText("Turn: " + game.getTurn());


    }

    private void applyEffects(EventResult evtResult) {
        for (EventEffect e : evtResult.getEffects()) {
            int current = 0;
            int change = e.getDelta();
            current = yourNation.getValue(e.getStat());
            yourNation.setValue(e.getStat(), current + change);
        }
    }
}

package ca.acoii.nationbuilder.nationbuilder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.scene.control.*;

import java.io.IOException;

public class App extends Application implements NationListener, GameEngineListener {
    private Label goldLabel, stabLabel, turnLabel;
    private StackPane rootStack;
    private Nation yourNation = new Nation();
    private GameEngine engine = new GameEngine(yourNation);

    @Override
    public void start(Stage stage) throws IOException {
        //Main Screen
        BorderPane root = new BorderPane();

        yourNation.addListener(this);
        engine.addListener(this);
        engine.loadEvents();

        //Resource items
        goldLabel = new Label();
        stabLabel = new Label();
        turnLabel = new Label();
        HBox resourcePane = new HBox(10, goldLabel, stabLabel, turnLabel);
        resourcePane.setAlignment(Pos.CENTER);
        resourcePane.setPadding(new Insets(10,20,10,20));
        //Menu Items
        Button btn1 = new Button("Construction");
        btn1.setPrefWidth(100);
        Button btn2 = new Button("Trade");
        btn2.setPrefWidth(100);
        Button btn3 = new Button("Diplomacy");
        btn3.setPrefWidth(100);
        Button btn4 = new Button("War");
        btn4.setPrefWidth(100);
        TilePane menuPane = new TilePane(10, 10, btn1, btn2, btn3, btn4);
        menuPane.setPrefColumns(3);
        menuPane.setAlignment(Pos.CENTER);
        //Bottom Button
        Button turnBtn = new Button("NEXT TURN");
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
        stage.setScene(new Scene(rootStack, 500, 359));
        onNationChanged(yourNation);
        onTurnAdvanced(engine);
        stage.show();

        //Testing Area
        for (GameEvent evt : engine.getEventList()) {
            System.out.println("Event: " + evt.getText());
        }
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

    @Override
    public void onNationChanged(Nation nation) {
        goldLabel.setText("Gold: " + nation.getValue(Stat.GOLD));
        stabLabel.setText("Stability: " + nation.getValue(Stat.STABILITY));
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

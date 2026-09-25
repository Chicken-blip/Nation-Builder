package ca.acoii.nationbuilder.nationbuilder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.control.*;

import java.io.IOException;

public class App extends Application implements NationListener, GameEngineListener {
    private Label goldLabel, stabLabel, turnLabel;
    private Nation yourNation = new Nation();
    private GameEngine engine = new GameEngine(yourNation);

    @Override
    public void start(Stage stage) throws IOException {
        BorderPane root = new BorderPane();
        yourNation.addListener(this);
        engine.addListener(this);

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
        turnBtn.setOnAction(e -> engine.nextTurn());
        HBox bottomPane = new HBox(10, turnBtn);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(10,20,10,20));

        //Root layout
        root.setTop(resourcePane);
        root.setCenter(menuPane);
        root.setBottom(bottomPane);

        stage.setScene(new Scene(root, 500, 359));
        onNationChanged(yourNation);
        onTurnAdvanced(engine);
        stage.show();

        //Testing Area
        engine.loadEvents();
        System.out.println("No. of events: " + engine.getEventListSize());

    }

    @Override
    public void onNationChanged(Nation nation) {
        goldLabel.setText("Gold: " + nation.getGold());
        stabLabel.setText("Stability: " + nation.getStability());
    }

    @Override
    public void onTurnAdvanced(GameEngine game) {
        turnLabel.setText("Turn: " + game.getTurn());
    }
}

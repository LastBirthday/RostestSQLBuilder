package com.home;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by Dds on 18.06.2016.
 */
public class InputSQLStage {

    private Stage stage;
    private Scene scene;

    private String text;

    private TextArea area;
    private Button cancel;
    private Button accept;

    public InputSQLStage(String text) {
        this.text = text;
    }

    public String display() {
        //Set up stage
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Окно редактирования SQL запроса");
        stage.setResizable(true);

        //Set up elements
        area = new TextArea();
        area.setWrapText(true);
        area.setText(text);


        cancel = new Button("Отмена");
        cancel.setOnAction(e -> {
            area.setText(text);
            stage.close();
        });

        accept = new Button("Готово");
        accept.setOnAction(e -> stage.close());

        //Set up Layout
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10,0,0,0));
        hBox.getChildren().addAll(cancel, accept);

        BorderPane pane = new BorderPane();
        pane.setCenter(area);
        pane.setBottom(hBox);
        pane.setPadding(new Insets(10,10,10,10));

        scene = new Scene(pane, 550, 500);
        scene.getStylesheets().add("Styles.css");

        stage.setScene(scene);
        stage.showAndWait();

        return area.getText();
    }

}

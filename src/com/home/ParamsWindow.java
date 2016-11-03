package com.home;

import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigInteger;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Dds on 11.04.2016.
 */
public class ParamsWindow {

    private Stage stage;
    private Scene scene;

    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    private ArrayList<TextField> textFieldsList;
    //private ArrayList<Label> errorLabels;
    private HashMap<Integer, Label> errorLabels;
    private String sql;
    private String headerText;

    public String display(String name, Label headerLabel, HashMap<String, Object> parameters) {
        textFieldsList = new ArrayList<>();
        errorLabels = new HashMap<>();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Окно установки параметров запроса");
        stage.setResizable(false);

        GridPane grid = new GridPane();
        grid.setVgap(5);
        grid.setHgap(12);
        grid.setAlignment(Pos.CENTER);
        ColumnConstraints column1 = new ColumnConstraints(250);
        ColumnConstraints column2 = new ColumnConstraints(250);
        grid.getColumnConstraints().addAll(column1, column2);

        //Set up top label
        Label topLabel = new Label("Укажите значения всех необходимых для выполнения запроса параметров:");
        GridPane.setConstraints(topLabel, 0, 0, 2, 1);
        GridPane.setMargin(topLabel, new Insets(0,0,15,0));
        GridPane.setHalignment(topLabel, HPos.CENTER);
        grid.getChildren().add(topLabel);

        name = name.replace(" ", "_");
        Properties prop = Queries.getPropertyFile();
        int params = Integer.parseInt(prop.getProperty(name + ".paramsCount"));
        int shift = 1;
        for (int i = 0; i < params; i++) {
            //Generate Labels
            Label label = new Label();
            label.setText(prop.getProperty(name + ".param." + i + ".translation"));
            GridPane.setConstraints(label, 0, i + shift);
            GridPane.setMargin(label, new Insets(0,0,-5,0));
            GridPane.setHalignment(label, HPos.RIGHT);

            //Generate TextFields
            TextField field = new TextField();
            field.setPromptText("Введите значение параметра");
            Tooltip tooltip = new Tooltip();
            switch (prop.getProperty(name + ".param." + i + ".type")) {
                case "String":
                    tooltip.setText("Пример: \"Иванов Сергей\"");
                    break;
                case "Integer":
                    tooltip.setText("Пример: \"10\"");
                    break;
                case "Double":
                    tooltip.setText("Пример: \"10.25\"");
                    break;
                case "Date":
                    tooltip.setText("Пример: \"01.06.2016\"");
                    break;
            }
            field.setTooltip(tooltip);
            final int pos = i;
            field.setOnMousePressed(e -> {
                field.pseudoClassStateChanged(errorClass, false);
                grid.getChildren().remove(errorLabels.get(pos));
                errorLabels.remove(pos);
            });
            field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    field.pseudoClassStateChanged(errorClass, false);
                    grid.getChildren().remove(errorLabels.get(pos));
                    errorLabels.remove(pos);
                }
            });
            textFieldsList.add(field);
            GridPane.setConstraints(field, 1, i + shift);
            GridPane.setMargin(field, new Insets(0,0,-5,0));

            grid.getChildren().addAll(label, field);
            shift++;
        }

        //Create buttons
        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> {
            stage.close();
        });

        Button finishButton = new Button("Выполнить");
        final String queryName = name;
        finishButton.setOnAction(e -> {
            int shiift = 2;
            boolean flag = true;
            for (int i = 0; i < params; i++) {
                if (textFieldsList.get(i).getText().equals("")) {
                    textFieldsList.get(i).pseudoClassStateChanged(errorClass, true);
                    if (errorLabels.get(i) == null) {
                        Label errorLabel = new Label("Поле параметра не может быть пустым!");
                        errorLabel.setTextFill(Color.RED);
                        GridPane.setConstraints(errorLabel, 0, i + shiift, 2, 1);
                        GridPane.setHalignment(errorLabel, HPos.RIGHT);
                        grid.getChildren().add(errorLabel);
                        errorLabels.put(i, errorLabel);
                    } else {
                        errorLabels.get(i).setText("Поле параметра не может быть пустым!");
                    }
                    flag = false;
                }
                switch (prop.getProperty(queryName + ".param." + i + ".type")) {
                    case "String":
                        /*if (textFieldsList.get(i).getText().contains("'")) {
                            textFieldsList.get(i).pseudoClassStateChanged(errorClass, true);
                            if (errorLabels.get(i) == null) {
                                Label errorLabel = new Label("Параметр-строка не может содержать символа ';'!");
                                errorLabel.setTextFill(Color.RED);
                                GridPane.setConstraints(errorLabel, 0, i + shiift, 2, 1);
                                GridPane.setHalignment(errorLabel, HPos.RIGHT);
                                grid.getChildren().add(errorLabel);
                                errorLabels.put(i, errorLabel);
                            } else {
                                errorLabels.get(i).setText("Параметр-строка не может содержать символа ';'!");
                            }
                            flag = false;
                        }*/
                        break;
                    case "Integer":
                        try {
                            BigInteger bigInt = new BigInteger(textFieldsList.get(i).getText());
                            //Integer.parseInt(textFieldsList.get(i).getText());
                        } catch (NumberFormatException e1) {
                            textFieldsList.get(i).pseudoClassStateChanged(errorClass, true);
                            if (errorLabels.get(i) == null) {
                                Label errorLabel = new Label("Данный параметр должен содержать ЦЕЛОЕ число! Пример: \"10\"");
                                errorLabel.setTextFill(Color.RED);
                                GridPane.setConstraints(errorLabel, 0, i + shiift, 2, 1);
                                GridPane.setHalignment(errorLabel, HPos.RIGHT);
                                grid.getChildren().add(errorLabel);
                                errorLabels.put(i, errorLabel);
                            } else {
                                errorLabels.get(i).setText("Данный параметр должен содержать ЦЕЛОЕ число! Пример: \"10\"");
                            }
                            flag = false;
                        }
                        break;
                    case "Double":
                        try {
                            Double.parseDouble(textFieldsList.get(i).getText());
                        } catch (NumberFormatException e1) {
                            textFieldsList.get(i).pseudoClassStateChanged(errorClass, true);
                            if (errorLabels.get(i) == null) {
                                Label errorLabel = new Label("Данный параметр должен содержать число! Пример: \"10.25\"");
                                errorLabel.setTextFill(Color.RED);
                                GridPane.setConstraints(errorLabel, 0, i + shiift, 2, 1);
                                GridPane.setHalignment(errorLabel, HPos.RIGHT);
                                grid.getChildren().add(errorLabel);
                                errorLabels.put(i, errorLabel);
                            } else {
                                errorLabels.get(i).setText("Данный параметр должен содержать число! Пример: \"10.25\"");
                            }
                            flag = false;
                        }
                        break;
                    case "Date":
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        sdf.setLenient(false);
                        ParsePosition position = new ParsePosition(0);
                        sdf.parse(textFieldsList.get(i).getText(), position);
                        if (position.getIndex() != textFieldsList.get(i).getText().length()) {
                            textFieldsList.get(i).pseudoClassStateChanged(errorClass, true);
                            if (errorLabels.get(i) == null) {
                                Label errorLabel = new Label("Данный параметр должен содержать дату! Пример: \"01.06.2016\"");
                                errorLabel.setTextFill(Color.RED);
                                GridPane.setConstraints(errorLabel, 0, i + shiift, 2, 1);
                                GridPane.setHalignment(errorLabel, HPos.RIGHT);
                                grid.getChildren().add(errorLabel);
                                errorLabels.put(i, errorLabel);
                            } else {
                                errorLabels.get(i).setText("Данный параметр должен содержать дату! Пример: \"01.06.2016\"");
                            }
                            flag = false;
                            System.out.println(position);
                        }
                        break;
                }
                shiift++;
            }
            if (flag) {
                parameters.clear();
                sql = prop.getProperty(queryName + ".query");
                headerText = prop.getProperty(queryName + ".header");
                SQLbuilder.queryName = queryName;
                for (int i = 0; i < params; i++) {
                    String parameter = "$P{" + prop.getProperty(queryName + ".param." + i) + "}";
                    switch (prop.getProperty(queryName + ".param." + i + ".type")) {
                        case "String":
                            /*sql = sql.substring(0, pos - 1).trim() + " '" + textFieldsList.get(i).getText() +
                                    "' " + sql.substring(pos + 1, sql.length()).trim();*/
                            //sql = sql.replaceAll(parameter, "'" + textFieldsList.get(i).getText() + "'");
                            if (textFieldsList.get(i).getText().contains("'"))
                                textFieldsList.get(i).setText(textFieldsList.get(i).getText().replaceAll("'", "''"));
                            sql = sql.replace(parameter, textFieldsList.get(i).getText());
                            parameters.put(prop.getProperty(queryName + ".param." + i), textFieldsList.get(i).getText());
                            break;
                        case "Integer":
                            /*sql = sql.substring(0, pos - 1).trim() + " " + textFieldsList.get(i).getText() +
                                    " " + sql.substring(pos + 1, sql.length()).trim();*/
                            sql = sql.replace(parameter, textFieldsList.get(i).getText());
                            parameters.put(prop.getProperty(queryName + ".param." + i), textFieldsList.get(i).getText());
                            break;
                        case "Double":
                            /*sql = sql.substring(0, pos - 1).trim() + " " + textFieldsList.get(i).getText() +
                                    " " + sql.substring(pos + 1, sql.length()).trim();*/
                            sql = sql.replace(parameter, textFieldsList.get(i).getText());
                            parameters.put(prop.getProperty(queryName + ".param." + i), textFieldsList.get(i).getText());
                            break;
                        case "Date":
                            /*sql = sql.substring(0, pos - 1).trim() + " '" + textFieldsList.get(i).getText() +
                                    "' " + sql.substring(pos + 1, sql.length()).trim();*/
                            sql = sql.replace(parameter, "'" + textFieldsList.get(i).getText() + "'");
                            parameters.put(prop.getProperty(queryName + ".param." + i), textFieldsList.get(i).getText());
                            //sql = sql.replaceAll(parameter, textFieldsList.get(i).getText());
                            break;
                    }
                    //int pos2 = headerText.indexOf('?');
                    /*headerText = headerText.substring(0, pos2 - 1).trim() + " " + textFieldsList.get(i).getText() +
                            " " + headerText.substring(pos2 + 1, headerText.length()).trim();*/
                    headerText = headerText.replace(parameter, textFieldsList.get(i).getText());
                }
                //sql = sql.trim();
                //headerText = headerText.trim();
                System.out.println(sql);
                System.out.println(headerText);
                headerText = headerText.replace("''", "'");
                headerLabel.setText(headerText);
                stage.close();
            }
        });

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(10);
        hBox.getChildren().addAll(cancelButton, finishButton);
        GridPane.setConstraints(hBox, 1, params + shift);
        GridPane.setMargin(hBox, new Insets(15,0,0,0));
        grid.getChildren().add(hBox);

        //System.out.println(prop.getProperty(name + ".query") + " count: " + params);

        scene = new Scene(grid, 520, 90 + 50 * params);
        scene.getStylesheets().add("Styles.css");

        //Set up stage
        stage.setScene(scene);
        stage.showAndWait();

        return sql;

    }

}

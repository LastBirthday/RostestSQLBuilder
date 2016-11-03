package com.home;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dds on 06.04.2016.
 */
public class QueryWindow {

    private Stage stage;
    private Scene scene;

    private Label nameLabel;
    private Label queryLabel;
    private Label resultLabel;

    private TextField nameField;
    private TextArea queryField;
    //private TextField queryField;
    private TextField resultField;

    private String result;

    private Label nameFieldError;
    private Label queryFieldError;
    private Label resultFieldError;
    private Label parametersError;

    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    /*private int params;
    private ArrayList<Label> labelsList;
    private ArrayList<TextField> textFieldsList;
    private ArrayList<ComboBox> comboBoxesList;*/

    private TableView<Param> tableView;
    private ObservableList<Param> parameters;

    private boolean isReport = false;
    private File reportFile;

    public String display() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Окно создания нового запроса");
        stage.setResizable(false);

        GridPane grid = new GridPane();
        grid.setVgap(20);
        grid.setHgap(12);
        grid.setAlignment(Pos.CENTER);
        ColumnConstraints column1 = new ColumnConstraints(140);
        ColumnConstraints column2 = new ColumnConstraints(350);
        grid.getColumnConstraints().addAll(column1, column2);

        //Set up Labels
        nameLabel = new Label("Имя запроса:");
        queryLabel = new Label("Запрос SQL:");
        resultLabel = new Label("Текст заголовка:");

        //Set up TextFields
        nameField = new TextField();
        nameField.setPromptText("Имя-описание запроса");
        Tooltip nameFieldTooltip = new Tooltip();
        nameFieldTooltip.setText("Пример: Найти все записи о приборах по указанному имени");
        nameField.setTooltip(nameFieldTooltip);
        nameField.setOnMousePressed(e -> {
            nameField.pseudoClassStateChanged(errorClass, false);
            grid.getChildren().remove(nameFieldError);
            nameFieldError = null;
        });
        nameField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                nameField.pseudoClassStateChanged(errorClass, false);
                grid.getChildren().remove(nameFieldError);
                nameFieldError = null;
            }
        }));
        //queryField = new TextField();
        queryField = new TextArea();
        queryField.setPrefRowCount(12);
        queryField.setWrapText(true);
        queryField.setPromptText("SQL запрос");
        Tooltip queryFieldTooltip = new Tooltip();
        queryFieldTooltip.setText("Пример: Select * from SDB_MAIN where name = $P{name}, где $P{name} параметр");
        queryField.setTooltip(queryFieldTooltip);
        queryField.setOnMousePressed(e -> {
            queryField.pseudoClassStateChanged(errorClass, false);
            grid.getChildren().remove(queryFieldError);
            queryFieldError = null;
        });
        queryField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                queryField.pseudoClassStateChanged(errorClass, false);
                grid.getChildren().remove(queryFieldError);
                queryFieldError = null;
            }
        }));
        queryField.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (e.getClickCount() == 2) {
                    InputSQLStage SQLstage = new InputSQLStage(queryField.getText());
                    queryField.setText(SQLstage.display());
                }
            }
        });
        resultField = new TextField();
        resultField.setPromptText("Текст заголовка результата");
        Tooltip resultFieldTooltip = new Tooltip();
        resultFieldTooltip.setText("Пример: Выведена полная иформация о записях, содержащих прибор $P{name}, где $P{name} это значение параметра, установеленное пользователем");
        resultField.setTooltip(resultFieldTooltip);
        resultField.setOnMousePressed(e -> {
            resultField.pseudoClassStateChanged(errorClass, false);
            grid.getChildren().remove(resultFieldError);
            resultFieldError = null;
        });
        resultField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                resultField.pseudoClassStateChanged(errorClass, false);
                grid.getChildren().remove(resultFieldError);
                resultFieldError = null;
            }
        }));

        //Set up TableView
        tableView = new TableView<>();
        tableView.setEditable(true);
        TableColumn<Param, String> firstColumn = new TableColumn<>("Параметр");
        firstColumn.setMinWidth(80);
        firstColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Param, String> secondColumn = new TableColumn<>("Расшифровка");
        secondColumn.setMinWidth(310);
        secondColumn.setCellValueFactory(new PropertyValueFactory<>("translation"));
        TableColumn<Param, String> thirdColumn = new TableColumn<>("Тип");
        thirdColumn.setMinWidth(120);
        thirdColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        parameters = FXCollections.observableArrayList();
        tableView.setItems(parameters);
        tableView.getColumns().addAll(firstColumn, secondColumn, thirdColumn);

        //Set up TableView Controls
        TextField nameTableField, translationTableField;
        nameTableField = new TextField();
        nameTableField.setPromptText("Параметр");
        nameTableField.setMinWidth(70);
        nameTableField.setOnMousePressed(e -> {
            nameTableField.pseudoClassStateChanged(errorClass, false);
            grid.getChildren().remove(parametersError);
            parametersError = null;
        });
        nameTableField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                nameTableField.pseudoClassStateChanged(errorClass, false);
                grid.getChildren().remove(parametersError);
                parametersError = null;
            }
        }));
        translationTableField = new TextField();
        translationTableField.setPromptText("Расшифровка");
        translationTableField.setMinWidth(90);
        translationTableField.setOnMousePressed(e -> {
            translationTableField.pseudoClassStateChanged(errorClass, false);
            grid.getChildren().remove(parametersError);
            parametersError = null;
        });
        translationTableField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                translationTableField.pseudoClassStateChanged(errorClass, false);
                grid.getChildren().remove(parametersError);
                parametersError = null;
            }
        }));
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(
                "String",
                "Integer",
                "Double",
                "Date"
        );
        box.setPromptText("Тип поля");
        box.setMinWidth(100);
        box.setOnMousePressed(e -> {
            box.pseudoClassStateChanged(errorClass, false);
            grid.getChildren().remove(parametersError);
            parametersError = null;
        });
        box.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                box.pseudoClassStateChanged(errorClass, false);
                grid.getChildren().remove(parametersError);
                parametersError = null;
            }
        }));

        Button add = new Button("Добавить");
        add.getStyleClass().add("button-long");
        add.setOnAction(e -> {
            if (nameTableField.getText().equals("")) nameTableField.pseudoClassStateChanged(errorClass, true);
            if (translationTableField.getText().equals("")) translationTableField.pseudoClassStateChanged(errorClass, true);
            if (box.getValue() == null) box.pseudoClassStateChanged(errorClass, true);
            if (!nameTableField.getText().equals("") && !translationTableField.getText().equals("") && box.getValue() != null) {
                Param param = new Param(nameTableField.getText(), translationTableField.getText(), box.getValue());
                parameters.add(param);
                nameTableField.clear();
                translationTableField.clear();
                box.getSelectionModel().clearSelection();
            }
            grid.getChildren().remove(parametersError);
            parametersError = null;
        });

        Button delete = new Button("Удалить");
        delete.getStyleClass().add("button-long");
        delete.setOnAction(e -> {
            ObservableList<Param> paramsSelected;
            paramsSelected = tableView.getSelectionModel().getSelectedItems();
            for (Param param : paramsSelected) {
                parameters.remove(param);
            }
            grid.getChildren().remove(parametersError);
            parametersError = null;
        });

        //Set up Buttons
        Button generateFromReportButton = new Button("   Генерация по отчёту   ");
        generateFromReportButton.getStyleClass().add("button-long");
        generateFromReportButton.setOnAction(e -> {
            reportFile = openFileChooser(stage, "Выберите отчёт", "Файл отчёта", "*.jrxml");
            if (reportFile != null) {
                queryField.setText(parseReport(reportFile, grid));
            }
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> {
            nameField.setText("");
            stage.close();
        });
        Button nextButton = new Button("Готово");
        nextButton.setOnAction(e -> {
            if (nameField.getText().equals("")) {
                nameField.pseudoClassStateChanged(errorClass, true);
                if (nameFieldError == null) {
                    nameFieldError = new Label("Введите имя запроса. Пример: \"Найти все записи о приборах по указанному имени\"");
                    nameFieldError.setTextFill(Color.RED);
                    GridPane.setConstraints(nameFieldError, 0, 1, 2, 1);
                    GridPane.setHalignment(nameFieldError, HPos.RIGHT);
                    grid.getChildren().add(nameFieldError);
                }
            }
            if (queryField.getText().equals("")) {
                queryField.pseudoClassStateChanged(errorClass, true);
                if (queryFieldError == null) {
                    queryFieldError = new Label("Введите запрос. Пример: \"select * from SDB_MAIN where id = $P{id}\", где $P{id} параметр");
                    queryFieldError.setTextFill(Color.RED);
                    GridPane.setConstraints(queryFieldError, 0, 3, 2, 1);
                    GridPane.setHalignment(queryFieldError, HPos.RIGHT);
                    grid.getChildren().add(queryFieldError);
                }
            }
            if (resultField.getText().equals("")) {
                resultField.pseudoClassStateChanged(errorClass, true);
                if (resultFieldError == null) {
                    resultFieldError = new Label("Введите заголовок. Пример: \"Выведены все записи с идентификатором $P{id}\"");
                    resultFieldError.setTextFill(Color.RED);
                    GridPane.setConstraints(resultFieldError, 0, 5, 2, 1);
                    GridPane.setHalignment(resultFieldError, HPos.RIGHT);
                    grid.getChildren().add(resultFieldError);
                }
            }
            if (!testParametersCount()) {
                if (parametersError == null) {
                    parametersError = new Label();
                    parametersError.setWrapText(true);
                    parametersError.setText("Внесите все параметры SQL запроса в таблицу. Например, $P{id} вносится как id. Либо " +
                            System.getProperty("line.separator") + "удалите не существующие параметры из таблицы.");
                    parametersError.setTextFill(Color.RED);
                    GridPane.setConstraints(parametersError, 0, 7, 2, 1);
                    GridPane.setHalignment(parametersError, HPos.RIGHT);
                    grid.getChildren().add(parametersError);
                }
            }
            if (!nameField.getText().equals("") && !queryField.getText().equals("") && !resultField.getText().equals("") && testParametersCount()) {
                updateProperty();
                result = nameField.getText();
                stage.close();
            }
        });

        //Set up Layout
        GridPane.setConstraints(nameLabel, 0, 0);
        GridPane.setMargin(nameLabel, new Insets(0,0,-15,0));
        GridPane.setConstraints(queryLabel, 0, 2);
        GridPane.setMargin(queryLabel, new Insets(-20,0,-20,0));
        GridPane.setConstraints(resultLabel, 0, 4);
        GridPane.setMargin(resultLabel, new Insets(-20,0,-20,0));
        GridPane.setConstraints(nameField, 1, 0);
        GridPane.setMargin(nameField, new Insets(0,0,-15,0));
        GridPane.setConstraints(queryField, 1, 2);
        GridPane.setMargin(queryField, new Insets(-20,0,-20,0));
        GridPane.setConstraints(resultField, 1, 4);
        GridPane.setMargin(resultField, new Insets(-20,0,-20,0));
        GridPane.setConstraints(tableView, 0, 6, 2, 1);
        GridPane.setMargin(tableView, new Insets(-15,0,-15,0));

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        hBox1.setSpacing(10);
        hBox1.getChildren().addAll(nameTableField, translationTableField, box, add, delete);
        GridPane.setConstraints(hBox1, 0, 8, 2, 1);
        GridPane.setMargin(hBox1, new Insets(-15,0,0,0));

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER_LEFT);
        hBox2.getChildren().addAll(generateFromReportButton);
        GridPane.setConstraints(hBox2, 0, 9);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(10);
        hBox.getChildren().addAll(cancelButton, nextButton);
        GridPane.setConstraints(hBox, 1, 9);

        grid.getChildren().addAll(nameLabel, nameField, queryLabel, queryField, resultLabel, resultField, tableView, hBox1, hBox2, hBox);
        grid.setPadding(new Insets(10,0,10,0));

        scene = new Scene(grid, 550, 500);
        scene.getStylesheets().add("Styles.css");

        //Set up stage
        stage.setScene(scene);
        stage.showAndWait();

        return result;

    }

    private File openFileChooser(Stage stage, String title, String extensionName, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(extensionName, extension)
        );
        return fileChooser.showOpenDialog(stage);
    }

    private String parseReport(File report, GridPane grid) {
        String query = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(report), "utf-8"))) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.trim().equals("<queryString>")) {
                    while (!(s = reader.readLine()).trim().equals("</queryString>")) {
                        query = query + " " + s;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (query.equals("")) {
            isReport = false;
            queryField.pseudoClassStateChanged(errorClass, true);
            if (queryFieldError == null) {
                queryFieldError = new Label("Обнаружить SQL запрос в файле отчёта не удалось. Генерация прошла не успешно.");
                queryFieldError.setTextFill(Color.RED);
                GridPane.setConstraints(queryFieldError, 0, 3, 2, 1);
                GridPane.setHalignment(queryFieldError, HPos.RIGHT);
                grid.getChildren().add(queryFieldError);
            }
        } else {
            queryField.pseudoClassStateChanged(errorClass, false);
            grid.getChildren().remove(queryFieldError);
            queryFieldError = null;

            query = query.trim();
            query = query.replace("<![CDATA[", "");
            query = query.replace("]]>", "");
            query = query.replace("$P!{", "$P{");
            isReport = true;
            System.out.println(query);
        }
        return query;
    }

    private void updateProperty() {
        Properties prop = Queries.getPropertyFile();

        //Make proper name for Query
        String newPropertyName = nameField.getText().replace(' ', '_');

        //Add props
        try (OutputStream out = new FileOutputStream(Queries.propFileName)) {
            prop.setProperty(newPropertyName + ".query", queryField.getText());
            prop.setProperty(newPropertyName + ".header", resultField.getText());
            prop.setProperty(newPropertyName + ".paramsCount", String.valueOf(parameters.size()));
            if (isReport) prop.setProperty(newPropertyName + ".report", reportFile.getAbsolutePath());
            for (int i = 0; i < parameters.size(); i++) {
                prop.setProperty(newPropertyName + ".param." + i, parameters.get(i).getName());
                prop.setProperty(newPropertyName + ".param." + i + ".translation", parameters.get(i).getTranslation());
                prop.setProperty(newPropertyName + ".param." + i + ".type", parameters.get(i).getType());
            }
            prop.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean testParametersCount() {
        String forTest = queryField.getText();
        for (int i = 0; i < parameters.size(); i++) {
            String subs = "$P{" + parameters.get(i).getName() + "}";
            if (!forTest.contains(subs)) {
                System.out.println("was here sad face OP");
                return false;
            }
            forTest = forTest.replace(subs, "");
            System.out.println(i + " " + forTest);
        }
        return !forTest.contains("$");
    }

}

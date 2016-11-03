package com.home;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Dds on 02.04.2016.
 *
 * ---------------Change log 1.0.0-----------------
 * - First release;
 * ---------------Change log 1.0.1-----------------
 * - Query field type on TextArea have changed;
 * - Some text messages changes;
 * ---------------Change log 1.0.2-----------------
 * - Added InputSQLStage;
 * - TableView standard message label changed;
 * - Report system testing;
 * ---------------Change log 1.0.3-----------------
 * - Image gallery added!
 * - Reports system added!
 * ---------------Change log 1.0.4-----------------
 * - Parameters definition changed to $P{name}
 * - Generate SQL from report feature added
 */

public class SQLbuilder extends Application {

    ListView<String> listView;
    Label headerLabel;

    private ObservableList<String> listData;
    private ObservableList<ObservableList<String>> data;
    private TableView<ObservableList<String>> tableView;
    public HashMap<String, Object> parameters;
    private ContextMenu listContextMenu;

    private String sql;
    private String currentSQL;
    public static String queryName;

    final String listItemsFileName = System.getProperty("user.dir") + System.getProperty("file.separator") + "Items.txt";

    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    private final String version = "1.0.4";

    public static void main(String[] args) {

        launch(args);

    }

    @Override
    public void start(Stage stage) throws Exception {
        parameters = new HashMap<>();

        stage.setTitle("WorkARM 1.0.4");
        stage.getIcons().add(new Image("file:SQLicon.png"));
        stage.setOnCloseRequest(e -> {
            saveProperty(Queries.propFileName);
            copyItems();
            System.exit(0);
        });

        //configure Menu
        Menu fileMenu = new Menu("Файл");

        MenuItem connect = new MenuItem("Выбрать Базу Данных...");
        connect.setOnAction(e -> openFileChooser(stage));

        MenuItem directory = new MenuItem("Выбрать директорию с фотографиями...");
        directory.setOnAction(e -> openDirectoryChooser(stage));

        MenuItem exit = new MenuItem("Выход");
        exit.setOnAction(e -> {
            saveProperty(Queries.propFileName);
            copyItems();
            System.exit(0);
        });

        fileMenu.getItems().addAll(connect, directory, exit);

        Menu helpMenu = new Menu("Помощь");

        MenuItem help = new MenuItem("О программе");
        help.setOnAction(e -> openTutotial());

        helpMenu.getItems().addAll(help);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, helpMenu);

        //configure Buttons
        Button printButton = new Button("Печать");
        Button exportButton = new Button("Экспорт");
        Button createQueryButton = new Button("Создать новый запрос");
        Button reportButton = new Button("Сгенерировать отчет");
        Button imageButton = new Button("Показать фотографии");

        createQueryButton.setOnAction(e -> {
            QueryWindow window = new QueryWindow();
            String listItem = window.display();
            if (listItem != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(listItemsFileName, true))) {
                    writer.write(listItem);
                    writer.newLine();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                listData.add(listItem);
            }
        });

        exportButton.setOnAction(e -> {
            /*if (data != null) exportToCSV(stage, tableView, headerLabel);
            else {
                headerLabel.setText("Прежде чем экспортировать данные, выберете необходимый Вам запрос и выполните его.");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            }*/
            String forTest = "select type, end_date, date, pers.fam, pers.name, pers.otch, pers2.fam as fam2, pers2.name as name2, pers2.otch as otch2, pers2.pos from sdb_main, wdb_tsk, pers, pers as pers2 where (sdb_main.id = $P{id} or sdb_main.id = 10) and wdb_tsk.id = $P{id_final} and wdb_tsk.id_pers = pers.id and  pers2.pos = \"ведущий инженер по метрологии\";";
            if (forTest.contains("$P{" + "id" + "}")) System.out.println("yes");
            else System.out.println("no fuck this");
        });

        printButton.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job.showPrintDialog(stage)) {
                boolean success = job.printPage(tableView);
                if (success) job.endJob();
            }

        });

        /*reportButton.setOnAction(e -> {
            try {
                String title = "АКТ" + System.getProperty("line.separator") + "ПРИЕМА-ПЕРЕДАЧИ" +
                        System.getProperty("line.separator") + "свидетельств о поверке и протоколов";
                String summary =
                        "     Передал\t\t\t\t\t\t\tПринял" + System.getProperty("line.separator") +
                        "     от ФБУ \"Ростест-Москва\"\t\t\t\t\tот" + System.getProperty("line.separator") +
                        "     _________________/__________________/\t\t\t_________________/__________________/" + System.getProperty("line.separator") +
                        "     \"_____\"_________________2016 г.\t\t\t\t\"_____\"_________________2016 г.";
                JasperReportBuilder report = DynamicReports.report();
                StyleBuilder titleStyle = DynamicReports.stl.style().bold().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setBottomPadding(15);
                StyleBuilder columnHeaderStyle = DynamicReports.stl.style().setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).bold();
                StyleBuilder columnStyle = DynamicReports.stl.style().setBorder(DynamicReports.stl.pen1Point())
                        .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT).setLeftIndent(2);
                StyleBuilder summaryStyle = DynamicReports.stl.style().bold().setTopPadding(15);
                report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);
                report.setPageMargin(DynamicReports.margin().setLeft(50).setRight(50).setTop(50).setBottom(50));
                report.setColumnTitleStyle(columnHeaderStyle);
                report.setColumnStyle(columnStyle);
                report.addColumn(Columns.column("Идентификатор", "id", DataTypes.integerType()));
                report.addColumn(Columns.column("Класс", "class", DataTypes.stringType()));
                report.addColumn(Columns.column("Тип", "type", DataTypes.stringType()));
                report.addColumn(Columns.column("Регистрационный номер", "reg", DataTypes.stringType()));
                report.addColumn(Columns.column("Интервал", "interv", DataTypes.integerType()));
                report.addColumn(Columns.column("Сертификат №", "sertif", DataTypes.stringType()));
                report.addColumn(Columns.column("Дата окончания", "end_date", DataTypes.stringType()));
                report.title(DynamicReports.cmp.text(title).setStyle(titleStyle));
                report.addSummary(Components.text(summary).setStyle(summaryStyle));
                report.addPageFooter(Components.pageXofY());
                report.setDataSource("select id, class, type, reg, interv, sertif, end_date from sdb_main where id >= 0 and id <= 10000 and type like '%AC%'", connection);
                report.show(false);
            } catch (DRException e1) {
                e1.printStackTrace();
            }
        });*/

        reportButton.setOnAction(e -> {
            String sourceFileName;
            if (currentSQL == null) {
                headerLabel.setText("Сначала необходимо выполнить запрос.");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            }
            else if ((sourceFileName = Queries.getQuery(queryName + ".report")) == null) {
                headerLabel.setText("Для данного запроса не назначен отчёт. Нажмите правой кнопкой мыши на запрос и выберите поле \"Назначить отчёт.\"");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            } else {
                //HashMap<String, Object> parameters = new HashMap<>();
                /*parameters.put("start_id", "0");
                parameters.put("end_id", "10000");
                parameters.put("type_of", "AC");*/
                System.out.println(parameters.size());

                try {
                    JasperReport jasperReport = JasperCompileManager.compileReport(sourceFileName);
                    //JasperReport jasperReport = (JasperReport) JRLoader.loadObject(new File(sourceFileName));
                    /*JasperReport jasperReport = JasperCompileManager.compileReport(System.getProperty("user.dir") +
                            System.getProperty("file.separator") + "reports" + System.getProperty("file.separator") + "report1.jrxml");*/
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
                    headerLabel.setText("Отчёт был успешно сгенерирован!");
                    headerLabel.pseudoClassStateChanged(errorClass, false);
                    JasperViewer.viewReport(jasperPrint, false);
                } catch (JRException e1) {
                    headerLabel.pseudoClassStateChanged(errorClass, true);
                    headerLabel.setText("Во время генерации отчёта произошла ошибка! Вероятно, что-то не так с отчётом. Так же убедитесь, " +
                            "что к запросу был прикреплён нужный отчёт. Ошибка: " + e1);
                    e1.printStackTrace();
                }
            }
        });

        imageButton.setOnAction(e -> {
            if (currentSQL == null) {
                headerLabel.setText("Сначала выполните запрос.");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            }
            else if (!currentSQL.toLowerCase().contains("sdb_main")) {
                headerLabel.setText("Для данного вида данных не предусмотрен вывод фотографий.");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            } else if (tableView.getSelectionModel().getSelectedItem() == null) {
                headerLabel.setText("Пожалуйста, выберите одну позицию, для которой хотите просмотреть фотографии.");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            } else if (tableView.getSelectionModel().getSelectedIndices().size() == 1) {
                Pattern pattern = Pattern.compile("p\\d\\d\\d" + tableView.getSelectionModel().getSelectedItem().get(0) + "\\.\\w+");
                System.out.println(tableView.getSelectionModel().getSelectedItem().get(0));
                ImageGallery gallery = new ImageGallery();
                gallery.display(pattern, stage);
            } else {
                System.out.println(tableView.getSelectionModel().getSelectedItem());
                System.out.println(tableView.getSelectionModel().getSelectedIndices().size());
                headerLabel.setText("Пожалуйста, выберите только одну позицию, для которой хотите просмотреть фотографии.");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            }
        });

        //configure ListView
        listData = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(listItemsFileName))) {
            String s;
            while ((s = reader.readLine()) != null) {
                listData.add(s);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        listView = new ListView<>(listData);
        listView.setMinWidth(300);
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                Tooltip tooltip = new Tooltip();
                ListCell<String> cell = new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item);
                            tooltip.setText(item);
                            setTooltip(tooltip);
                        } else {
                            setText("");
                            setTooltip(null);
                        }
                    }
                };

                cell.setOnDragDetected(e -> {
                    if (cell.getItem() == null) {
                        return;
                    }
                    Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem());
                    dragboard.setContent(content);
                    e.consume();
                });

                cell.setOnDragOver(e -> {
                    if (e.getGestureSource() != cell && e.getDragboard().hasString()) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                    e.consume();
                });

                cell.setOnDragEntered(e -> {
                    if (e.getGestureSource() != cell && e.getDragboard().hasString()) {
                        cell.setOpacity(0.3);
                    }
                });

                cell.setOnDragExited(e -> {
                    if (e.getGestureSource() != cell && e.getDragboard().hasString()) {
                        cell.setOpacity(1);
                    }
                });

                cell.setOnDragDropped(e -> {
                    if (cell.getItem() == null) {
                        return;
                    }
                    Dragboard dragboard = e.getDragboard();
                    boolean success = false;
                    if (dragboard.hasString()) {
                        ObservableList<String> items = cell.getListView().getItems();
                        int draggedIdx = items.indexOf(dragboard.getString());
                        int thisIdx = items.indexOf(cell.getItem());
                        String tmp = listData.get(draggedIdx);
                        if (draggedIdx > thisIdx) {
                            for (int i = draggedIdx-1; i >= thisIdx; i--) {
                                listData.set(i+1, listData.get(i));
                            }
                            listData.set(thisIdx, tmp);
                        } else {
                            for (int i = draggedIdx+1; i <= thisIdx; i++) {
                                listData.set(i-1, listData.get(i));
                            }
                            listData.set(thisIdx, tmp);
                        }
                        success = true;
                    }
                    e.setDropCompleted(success);
                    e.consume();
                });

                cell.setOnDragDone(DragEvent::consume);

                listContextMenu = new ContextMenu();
                MenuItem delete = new MenuItem("Удалить запрос");
                delete.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Подтвердите удаление");
                    alert.setHeaderText("Вы уверены что хотите удалить данный запрос?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        String itemName = listView.getSelectionModel().getSelectedItem();
                        deleteItem(itemName);
                        for (int i = 0; i < listView.getItems().size(); i++) {
                            if (listView.getItems().get(i).equals(itemName))
                                listData.remove(i);
                        }
                        itemName = itemName.replace(" ", "_");
                        deleteProperty(itemName);
                    }
                });
                MenuItem change = new MenuItem("Редактировать запрос");
                change.setOnAction(e -> {
                    QueryChangeWindow window = new QueryChangeWindow(listView.getSelectionModel().getSelectedItem());
                    String listItem = window.display();
                    if (listItem != null) {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(listItemsFileName, true))) {
                            writer.write(listItem);
                            writer.newLine();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        listData.remove(listView.getSelectionModel().getSelectedItem());
                        deleteItem(listView.getSelectionModel().getSelectedItem());
                        listData.add(listItem);
                        //listView.getItems().add(listItem);
                    }
                });
                MenuItem setReport = new MenuItem("Назначить отчёт");
                setReport.setOnAction(e -> {
                    File reportFile = openFileChooser(stage, "Выберите отчёт", "Файл отчёта", "*.jrxml");
                    if (reportFile != null) {
                        String propName = listView.getSelectionModel().getSelectedItem().replace(' ', '_');
                        Properties prop = Queries.getPropertyFile();
                        try (OutputStream out = new FileOutputStream(Queries.propFileName)) {
                            prop.setProperty(propName + ".report", reportFile.getAbsolutePath());
                            prop.store(out, null);
                            headerLabel.setText("Отчёт был успешно назначен!");
                            headerLabel.pseudoClassStateChanged(errorClass, false);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                listContextMenu.getItems().addAll(change, setReport, delete);
                cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                    if (isNowEmpty) {
                        cell.setContextMenu(null);
                        cell.setOnMouseClicked(null);
                    } else {
                        cell.setContextMenu(listContextMenu);
                        cell.setOnMouseClicked(e -> {
                            if (e.getButton() == MouseButton.PRIMARY) {
                                ParamsWindow window = new ParamsWindow();
                                //parameters = new HashMap<>();
                                sql = window.display(listView.getSelectionModel().getSelectedItem(), headerLabel, parameters);
                                for (String key : parameters.keySet()) {
                                    System.out.println(key + " " + parameters.get(key));
                                }
                                if (sql != null) {
                                    currentSQL = sql;
                                    fillTable();
                                }
                            }
                        });
                    }
                });
                return cell;
            }
        });

        //configure Center
        headerLabel = new Label();
        headerLabel.setWrapText(true);
        headerLabel.setTextAlignment(TextAlignment.CENTER);
        headerLabel.setStyle("-fx-font-size: 14px");
        headerLabel.setText("Добро пожаловать в программу!");

        tableView = new TableView<>();
        tableView.setPlaceholder(new Label("Нет данных в таблице"));
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TableUtils.installCopyPasteHandler(tableView);
        tableView.setRowFactory(e -> {
            TableRow<ObservableList<String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    if (!currentSQL.toLowerCase().contains("sdb_main")) {
                        headerLabel.setText("Для данного вида данных не предусмотрен вывод фотографий.");
                        headerLabel.pseudoClassStateChanged(errorClass, false);
                    } else if (tableView.getSelectionModel().getSelectedItem() == null) {
                        headerLabel.pseudoClassStateChanged(errorClass, false);
                        headerLabel.setText("Пожалуйста, выберите одну позицию, для которой хотите просмотреть фотографии.");
                    } else if (tableView.getSelectionModel().getSelectedIndices().size() == 1) {
                        Pattern pattern = Pattern.compile("p\\d\\d\\d" + tableView.getSelectionModel().getSelectedItem().get(0) + "\\.\\w+");
                        System.out.println(tableView.getSelectionModel().getSelectedItem().get(0));
                        ImageGallery gallery = new ImageGallery();
                        gallery.display(pattern, stage);
                    } else {
                        System.out.println(tableView.getSelectionModel().getSelectedItem());
                        System.out.println(tableView.getSelectionModel().getSelectedIndices().size());
                        headerLabel.pseudoClassStateChanged(errorClass, false);
                        headerLabel.setText("Пожалуйста, выберите только одну позицию, для которой хотите просмотреть фотографии.");
                    }
                }
            });
            return row;
        });
        /*tableView.setRowFactory(e -> {
            TableRow row = new TableRow();
            tableContextMenu = new ContextMenu();
            MenuItem deleteRecord = new MenuItem("Удалить запись");
            deleteRecord.setOnAction(event -> deleteRow(tableView.getSelectionModel().getSelectedItems()));
            tableContextMenu.getItems().addAll(deleteRecord);
            row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) row.setContextMenu(null);
                else row.setContextMenu(tableContextMenu);
            });
            return row;
        });*/

        //configure layout
        HBox hBox = new HBox();
        hBox.getChildren().addAll(imageButton, reportButton, exportButton, createQueryButton);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(8);

        VBox topVBox = new VBox();
        topVBox.getChildren().addAll(menuBar, hBox);
        topVBox.setPadding(new Insets(0, 0, 1, 0));
        topVBox.setStyle("-fx-border-width: 1;" + "-fx-border-color: darkgrey;" +
                "-fx-border-style: hidden hidden solid hidden;");

        VBox centerVBox = new VBox();
        centerVBox.getChildren().addAll(headerLabel, tableView);
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setPadding(new Insets(8, 0, 0, 0));
        centerVBox.setSpacing(8);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        BorderPane border = new BorderPane();
        border.setTop(topVBox);
        border.setLeft(listView);
        border.setCenter(centerVBox);

        Scene scene = new Scene(border, 1400, 800);
        scene.getStylesheets().add("Styles.css");
        stage.setScene(scene);
        stage.show();

        //Check properties exists
        //Get connection to Data Base
        if (!new File("queries.properties").exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Критическая ошибка");
            alert.setHeaderText("Нарушена целостность программы.");
            alert.setContentText("В корне отсутствует файл настроек queries.properties. Восстановите его, либо обратитесь к разработчику.");
            alert.showAndWait();
            System.exit(1);
        } else {
            connectDataBase(Queries.getQuery("dbName"));
        }

    }

    /*private void deleteRow(ObservableList<ObservableList> row) {
        TableColumn column = (TableColumn) tableView.getColumns().get(0);
        String idColumn = column.getText();
        TablePosition pos = tableView.getSelectionModel().getSelectedCells().get(0);
        int rowPosition = pos.getRow();
        String id = (String) column.getCellObservableValue(rowPosition).getValue();
        String schemaName = sql.substring(sql.toLowerCase().indexOf(" from ") + 5).trim();
        if (schemaName.contains(" ")) schemaName = schemaName.substring(0, schemaName.indexOf(" ")).trim();
        String deleteSQL = "delete from " + schemaName + " where " + idColumn + " = " + id;
        System.out.println(deleteSQL);
        for (ObservableList list : row) {
            data.remove(list);
        }
        try {
            statement.executeUpdate(deleteSQL);
            headerLabel.pseudoClassStateChanged(errorClass, false);
        } catch (SQLException e) {
            e.printStackTrace();
            headerLabel.setText("По пока необъяснимой причине выполнить удаление записи из Базы Данных не удалось. " +
                    "Пожалуйста, обратитесь к разработчику.");
            headerLabel.pseudoClassStateChanged(errorClass, true);
        }
    }*/

    private void deleteProperty(String item) {
        Properties prop = Queries.getPropertyFile();
        try (OutputStream out = new FileOutputStream(Queries.propFileName)) {
            int params;
            if (prop.getProperty(item + ".paramsCount") == null) params = 0;
            else params = Integer.parseInt(prop.getProperty(item + ".paramsCount"));
            prop.remove(item + ".query");
            prop.remove(item + ".header");
            prop.remove(item + ".paramsCount");
            if (prop.getProperty(item + ".report") != null) prop.remove(item + ".report");
            for (int i = 0; i < params; i++) {
                prop.remove(item + ".param." + i);
                prop.remove(item + ".param." + i + ".translation");
                prop.remove(item + ".param." + i + ".type");
            }
            prop.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyItems() {
        File oldItems = new File(listItemsFileName);
        File newItems = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Items_tmp.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newItems))) {
            for (int i = 0; i < listData.size(); i++) {
                writer.write(listData.get(i));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (oldItems.delete()) {
            System.out.println("Old Items.txt deleted.");
            if (newItems.renameTo(oldItems)) System.out.println("Rename to Items.txt successful.");
        }
    }

    private void deleteItem(String item) {
        File oldItems = new File(listItemsFileName);
        File newItems = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Items_tmp.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(oldItems));
             BufferedWriter writer = new BufferedWriter(new FileWriter(newItems))) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (!s.equals(item)) {
                    writer.write(s);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (oldItems.delete()) {
            System.out.println("Old Items.txt deleted.");
            if (newItems.renameTo(oldItems)) System.out.println("Rename to Items.txt successful.");
        }
    }

    private void fillTable() {
        data = FXCollections.observableArrayList();
        tableView.getItems().removeAll(data);
        tableView.getColumns().remove(0, tableView.getColumns().size());
        try {
            if (connection == null) {
                headerLabel.setText("Не удалось обнаружить файл базы данных. Выберете меню Файл -> Выбрать Базу Данных");
                headerLabel.pseudoClassStateChanged(errorClass, true);
            } else {
                System.out.println(sql);
                statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
                ResultSetMetaData rsmd = resultSet.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    final int j = i - 1;
                    TableColumn<ObservableList<String>, String> column = new TableColumn<>(rsmd.getColumnName(i));
                    column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList<String>, String> param) {
                            return new SimpleStringProperty(param.getValue().get(j));
                        }
                    });
                    tableView.getColumns().add(column);
                }
                while (resultSet.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        if (resultSet.getString(i) == null) row.add("");
                        else row.add(resultSet.getString(i));
                        System.out.print(resultSet.getString(i) + " ");
                    }
                    data.add(row);
                    System.out.println();
                }
                tableView.setItems(data);
                headerLabel.pseudoClassStateChanged(errorClass, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            headerLabel.setText("Что-то не так с запросом, либо с Базой Данных. Чтобы выбрать другую Базу Данных " +
                    "нажмите Файл -> Выбрать Базу Данных... Если проблема не исчезла, обратитесь к разработчику. Ошибка: " + e);
            headerLabel.pseudoClassStateChanged(errorClass, true);
        }
    }

    private void connectDataBase(String dataBaseName) {
        if (dataBaseName == null) {
            headerLabel.setText("Не удалось обнаружить файл базы данных. Выберете меню Файл -> Выбрать Базу Данных");
            headerLabel.pseudoClassStateChanged(errorClass, true);
            return;
        }
        File database = new File(dataBaseName);
        if (!database.exists()) {
            headerLabel.setText("Не удалось обнаружить файл базы данных. Выберете меню Файл -> Выбрать Базу Данных");
            headerLabel.pseudoClassStateChanged(errorClass, true);
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataBaseName);
            } catch (ClassNotFoundException e) {
                headerLabel.setText("Драйвер базы данных не найден. Обратитесь к разработчику.");
                headerLabel.pseudoClassStateChanged(errorClass, true);
                e.printStackTrace();
            } catch (SQLException e) {
                headerLabel.setText("Соединение с базой данных установить не удалось. Обратитесь к разработчику.");
                headerLabel.pseudoClassStateChanged(errorClass, true);
                e.printStackTrace();
            }
        }
    }

    private void exportToCSV(Stage stage, TableView<ObservableList<String>> table, Label headerLabel) {
        final String separator = System.getProperty("line.separator");
        final String delimiter = "\t";
        File file = saveFileChooser(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "windows-1251"))) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    //writer.append('\"').append(table.getColumns().get(i).getText()).append('\"');
                    writer.append(table.getColumns().get(i).getText());
                    writer.append(delimiter);
                }
                writer.append(separator);
                for (int i = 0; i < data.size(); i++) {
                    ObservableList<String> row = data.get(i);
                    for (int j = 0; j < row.size(); j++) {
                        //writer.append('\"').append(row.get(j)).append('\"');
                        writer.append(row.get(j));
                        writer.append(delimiter);
                    }
                    writer.append(separator);
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            headerLabel.setText("Экспорт данных в Microsoft Excel CSV файл завершился. Файл был успешно сохранён!");
        }

    }

    private void saveProperty(String source) {
        File dest = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "queries_copy.properties");
        try (InputStream in = new FileInputStream(new File(source));
            OutputStream out = new FileOutputStream(dest)) {
            byte buffer[] = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File saveFileChooser(Stage stage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить файл");
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Текстовый txt файл", "*.txt")
                //new FileChooser.ExtensionFilter("Excel csv файл", "*.csv")
        );

        return fileChooser.showSaveDialog(stage);
    }

    private void openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите Базу Данных");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("База Данных", "*.db")
        );
        File file = fileChooser.showOpenDialog(stage);

        Properties prop = Queries.getPropertyFile();
        if (file != null) {
            try (OutputStream out = new FileOutputStream(Queries.propFileName)) {
                prop.setProperty("dbName", file.toString());
                prop.store(out, null);
                headerLabel.setText("Соединение с Базой Данных успешно установлено!");
                headerLabel.pseudoClassStateChanged(errorClass, false);
                connectDataBase(Queries.getQuery("dbName"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

    private void openDirectoryChooser(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Укажите путь к фотографиям");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = directoryChooser.showDialog(stage);

        Properties prop = Queries.getPropertyFile();
        if (file != null) {
            try (OutputStream out = new FileOutputStream(Queries.propFileName)) {
                prop.setProperty("photosFolder", file.toString());
                prop.store(out, null);
                headerLabel.setText("Папка с фотографиями успешно назначена!");
                headerLabel.pseudoClassStateChanged(errorClass, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void openTutotial() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Окно описания программы");
        stage.setResizable(true);

        WebView browser = new WebView();
        WebEngine engine = browser.getEngine();
        //engine.load(getClass().getResource("/resources/help.html").toExternalForm());
        engine.load("file:" + System.getProperty("file.separator") + System.getProperty("user.dir") + System.getProperty("file.separator") +
                "resources" + System.getProperty("file.separator") + "help.html");

        Scene scene = new Scene(browser, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    /*private JRDataSource createDataSource() {
        DRDataSource dataSource = new DRDataSource("item", "quantity", "unitprice");
        dataSource.add("Notebook", 1, new BigDecimal(500));
        dataSource.add("DVD", 5, new BigDecimal(30));
        dataSource.add("DVD", 1, new BigDecimal(28));
        dataSource.add("DVD", 5, new BigDecimal(32));
        dataSource.add("Book", 3, new BigDecimal(11));
        dataSource.add("Book", 1, new BigDecimal(15));
        dataSource.add("Book", 5, new BigDecimal(10));
        dataSource.add("Book", 8, new BigDecimal(9));
        return dataSource;
    }*/

}

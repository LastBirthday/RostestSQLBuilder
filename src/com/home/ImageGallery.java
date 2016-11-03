package com.home;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dds on 28.06.2016.
 */

public class ImageGallery {

    private Stage stage;
    private ArrayList<File> fileList = new ArrayList<>();

    boolean nextHappened = true;
    boolean previousHappened = false;

    public void display (Pattern pattern, Stage rootStage) {
        ScrollPane root = new ScrollPane();
        TilePane tile = new TilePane();
        root.setStyle("-fx-background-color: DAE6F3;");
        tile.setPadding(new Insets(15,15,15,15));
        tile.setHgap(15);

        File folder = new File(Queries.getQuery("photosFolder"));
        File[] listOfFiles = folder.listFiles();
        System.out.println(folder.getAbsoluteFile());

        if (listOfFiles == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Фотографии не найдены.");
            alert.setHeaderText("Путь к папке с фотографиями не найден!");
            alert.setContentText("Нажмите \"OK\" и в появившемся окне укажите путь к папке с фотографиями.");
            alert.showAndWait();

            openDirectoryChooser(rootStage);
            folder = new File(Queries.getQuery("photosFolder"));
            listOfFiles = folder.listFiles();
        }

        if (listOfFiles != null) {
            int count = 0;
            for (int i = 0; i < listOfFiles.length; i++) {
                Matcher matcher = pattern.matcher(listOfFiles[i].getName());
                if (matcher.matches()) {
                    System.out.println(listOfFiles[i].getName() + " matches!");
                    fileList.add(listOfFiles[i]);
                    ImageView imageView = createImageView(listOfFiles[i], count);
                    tile.getChildren().add(imageView);
                    count++;
                } else System.out.println(listOfFiles[i].getName() + " not matches!");
            }

            root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            root.setFitToWidth(true);
            root.setContent(tile);

            stage = new Stage();
            stage.setTitle("Предпросмотр фотографий.");
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
    }

    private ImageView createImageView(final File imageFile, int pos) {
        ImageView imageView = null;

        try {
            final Image image = new Image(new FileInputStream(imageFile), 150, 0, true, true);
            imageView = new ImageView(image);
            imageView.setFitWidth(150);
            imageView.setOnMouseClicked(e -> {
                if (e.getButton().equals(MouseButton.PRIMARY)) {
                    if (e.getClickCount() == 2) {
                        ListIterator<File> iterator = fileList.listIterator();
                        for (int i = 0; i <= pos; i++) {
                            iterator.next();
                        }
                        try {
                            Stage newStage = new Stage();
                            //Set up Buttons
                            /*Image imagePrev = new Image(new FileInputStream(new File(System.getProperty("user.dir") +
                                    System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") +
                                    "buttons_images" + System.getProperty("file.separator") + "prev.png")));
                            Image imageNext = new Image(new FileInputStream(new File(System.getProperty("user.dir") +
                                    System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") +
                                    "buttons_images" + System.getProperty("file.separator") + "next.png")));*/
                            Button next = new Button();
                            next.getStyleClass().add("button-round-right");
                            //next.setGraphic(new ImageView(imageNext));
                            next.setOnAction(e1 -> {

                            });
                            Button prev = new Button();
                            prev.getStyleClass().add("button-round-left");
                            //prev.setGraphic(new ImageView(imagePrev));

                            //Set up BorderPane
                            BorderPane borderPane = new BorderPane();
                            ImageView imageView1 = new ImageView();
                            Image image1 = new Image(new FileInputStream(imageFile));
                            imageView1.setImage(image1);
                            imageView1.setStyle("-fx-background-color: black");
                            imageView1.setPreserveRatio(true);
                            imageView1.setSmooth(true);
                            imageView1.setCache(true);
                            borderPane.setStyle("-fx-background-color: black");

                            HBox hBox = new HBox();
                            hBox.setAlignment(Pos.BOTTOM_CENTER);
                            hBox.setSpacing(10);
                            hBox.setPadding(new Insets(0,0,10,0));
                            hBox.getChildren().addAll(prev, next);

                            StackPane stackPane = new StackPane();
                            stackPane.getChildren().addAll(imageView1, hBox);

                            borderPane.setCenter(stackPane);

                            //Set up buttons controls
                            next.setOnAction(e1 -> {
                                try {
                                    if (iterator.hasNext()) {
                                        if (previousHappened) {
                                            iterator.next();
                                            previousHappened = false;
                                        }
                                        File nextFile = iterator.next();
                                        Image nextImage = new Image(new FileInputStream(nextFile));
                                        imageView1.setImage(nextImage);
                                        newStage.setTitle(nextFile.getName());
                                        nextHappened = true;
                                    }
                                } catch (FileNotFoundException e2) {
                                    e2.printStackTrace();
                                }
                            });

                            prev.setOnAction(e1 -> {
                                try {
                                    if (iterator.hasPrevious()) {
                                        if (nextHappened) {
                                            iterator.previous();
                                            nextHappened = false;
                                        }
                                        if (iterator.hasPrevious()) {
                                            File prevFile = iterator.previous();
                                            Image prevImage = new Image(new FileInputStream(prevFile));
                                            imageView1.setImage(prevImage);
                                            newStage.setTitle(prevFile.getName());
                                            previousHappened = true;
                                        } else {
                                            previousHappened = true;
                                        }
                                    }
                                } catch (FileNotFoundException e2) {
                                    e2.printStackTrace();
                                }
                            });

                            //Set up stage
                            newStage.setWidth(stage.getWidth());
                            newStage.setHeight(stage.getHeight());
                            newStage.setMinHeight(300);
                            newStage.setMinWidth(400);
                            newStage.setTitle(imageFile.getName());
                            Scene scene = new Scene(borderPane, Color.BLACK);
                            scene.getStylesheets().add("Styles.css");
                            newStage.setScene(scene);
                            imageView1.fitWidthProperty().bind(borderPane.widthProperty());
                            imageView1.fitHeightProperty().bind(borderPane.heightProperty());
                            newStage.initModality(Modality.APPLICATION_MODAL);
                            newStage.show();
                            nextHappened = true;
                            previousHappened = false;
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return imageView;
    }

    private void openDirectoryChooser(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Укажите путь к фотографиям");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        /*directoryChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("База Данных", "*.db")
        );*/
        File file = directoryChooser.showDialog(stage);

        Properties prop = Queries.getPropertyFile();
        if (file != null) {
            try (OutputStream out = new FileOutputStream(Queries.propFileName)) {
                prop.setProperty("photosFolder", file.toString());
                prop.store(out, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}

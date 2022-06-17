package com.home.filesorter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

//Welcome for collaboration

public class App extends Application {
    private double gapX = 0, gapY = 0;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxml/main.fxml")));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("File Sorter");
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
        scene.setOnMouseDragged(e -> this.dragStage(e, stage));
        scene.setOnMouseMoved(e -> this.calculateGap(e, stage));
        Image runtimeIcon = new Image("images/runtime_icon.png");
        if (runtimeIcon != null) {
            stage.getIcons().add(runtimeIcon);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private void calculateGap(MouseEvent event, Stage stage){
        gapX = event.getScreenX() - stage.getX();
        gapY = event.getScreenY() - stage.getY();
    }
    private void dragStage(MouseEvent event, Stage stage) {
        stage.setX(event.getScreenX() - gapX);
        stage.setY(event.getScreenY() - gapY);
    }
}

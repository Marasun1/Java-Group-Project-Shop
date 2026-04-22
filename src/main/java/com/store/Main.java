package com.store;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);

        scene.getStylesheets().add(
                Main.class.getResource("/styles/style.css").toExternalForm()
        );

        stage.setTitle("Warehouse Management System");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
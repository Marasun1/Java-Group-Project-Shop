package com.store;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Точка входу до desktop-застосунку складського обліку.
 * Завантажує головний JavaFX-інтерфейс, підключає глобальні стилі
 * та показує основне вікно програми.
 */
public class Main extends Application {

    /**
     * Ініціалізує та відображає головне вікно застосунку.
     *
     * @param stage головне вікно JavaFX
     * @throws Exception якщо не вдалося завантажити головний FXML-інтерфейс
     */
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

    /**
     * Запускає JavaFX-застосунок.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        launch(args);
    }
}

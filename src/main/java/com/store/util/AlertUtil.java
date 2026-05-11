package com.store.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Набір допоміжних методів для показу стандартних JavaFX-діалогів.
 */
public class AlertUtil {

    private AlertUtil() {
    }

    /**
     * Показує інформаційне діалогове вікно.
     *
     * @param title заголовок вікна
     * @param message текст повідомлення
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показує попереджувальне діалогове вікно.
     *
     * @param title заголовок вікна
     * @param message текст повідомлення
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показує діалогове вікно з помилкою.
     *
     * @param title заголовок вікна
     * @param message текст повідомлення
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Сталася помилка");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показує діалог підтвердження та повертає вибір користувача.
     *
     * @param title заголовок вікна
     * @param message текст повідомлення
     * @return {@code true}, якщо користувач підтвердив дію
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

package com.store.controller;

import com.store.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Button productsButton;

    @FXML
    private Button quantitiesButton;

    @FXML
    private Button receiptsButton;

    @FXML
    private Button pricesButton;

    @FXML
    private Button usersButton;

    @FXML
    private void initialize() {
        showProducts();
    }

    @FXML
    private void showProducts() {
        loadPage("/fxml/products-view.fxml", productsButton);
    }

    @FXML
    private void showQuantities() {
        loadPage("/fxml/quantities-view.fxml", quantitiesButton);
    }

    @FXML
    private void showReceipts() {
        loadPage("/fxml/receipts-view.fxml", receiptsButton);
    }

    @FXML
    private void showPrices() {
        loadPage("/fxml/prices-view.fxml", pricesButton);
    }

    @FXML
    private void showUsers() {
        loadPage("/fxml/users-view.fxml", usersButton);
    }

    private void loadPage(String fxmlPath, Button activeButton) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(page);
            setActiveButton(activeButton);
        } catch (IOException | RuntimeException e) {
            AlertUtil.showError("Помилка навігації", "Не вдалося завантажити сторінку: " + fxmlPath);
        }
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {
                productsButton,
                quantitiesButton,
                receiptsButton,
                pricesButton,
                usersButton
        };

        for (Button button : buttons) {
            button.getStyleClass().remove("active-nav-button");
        }

        activeButton.getStyleClass().add("active-nav-button");
    }
}

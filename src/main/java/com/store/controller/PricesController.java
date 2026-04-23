package com.store.controller;

import com.store.model.Price;
import com.store.service.PriceService;
import com.store.util.AlertUtil;
import com.store.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PricesController {

    @FXML private TableView<Price> priceTable;
    @FXML private TableColumn<Price, Long> idColumn;
    @FXML private TableColumn<Price, Long> productIdColumn;
    @FXML private TableColumn<Price, BigDecimal> amountColumn;
    @FXML private TableColumn<Price, BigDecimal> retailPriceColumn;
    @FXML private TableColumn<Price, LocalDateTime> validFromColumn;
    @FXML private TableColumn<Price, LocalDateTime> validToColumn;
    @FXML private TextField productIdField;
    @FXML private TextField amountField;
    @FXML private TextField retailPriceField;
    @FXML private TextField validToField;
    @FXML private Label statusLabel;

    private final PriceService priceService = new PriceService();
    private final ObservableList<Price> priceList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Long editingPriceId = null;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        retailPriceColumn.setCellValueFactory(new PropertyValueFactory<>("retailPrice"));
        validFromColumn.setCellValueFactory(new PropertyValueFactory<>("validFrom"));
        validToColumn.setCellValueFactory(new PropertyValueFactory<>("validTo"));
        configureDateColumn(validFromColumn);
        configureDateColumn(validToColumn);

        priceTable.setItems(priceList);
        priceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, price) -> {
            if (price != null) {
                fillForm(price);
            }
        });

        loadPrices();
    }

    @FXML
    private void handleSave() {
        try {
            Price price = new Price();
            price.setId(editingPriceId);
            price.setProductId(ValidationUtil.positiveLong(productIdField.getText(), "Товар ID"));
            price.setAmount(ValidationUtil.nonNegativeDecimal(amountField.getText(), "Закупівельна ціна"));
            price.setRetailPrice(ValidationUtil.nonNegativeDecimal(retailPriceField.getText(), "Роздрібна ціна"));
            price.setValidTo(ValidationUtil.optionalDateTime(validToField.getText(), "Діє до", formatter));

            if (editingPriceId == null) {
                priceService.createPrice(price);
                AlertUtil.showInfo("Успіх", "Ціну додано.");
            } else if (priceService.updatePrice(price)) {
                AlertUtil.showInfo("Успіх", "Ціну оновлено.");
            }

            loadPrices();
            clearForm();
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Перевірка", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Помилка збереження", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Price selected = priceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Видалення", "Спочатку вибери ціну у таблиці.");
            return;
        }

        if (!AlertUtil.showConfirmation("Підтвердження", "Видалити вибрану ціну?")) {
            return;
        }

        try {
            priceService.deletePrice(selected.getId());
            loadPrices();
            clearForm();
            AlertUtil.showInfo("Успіх", "Ціну видалено.");
        } catch (Exception e) {
            AlertUtil.showError("Помилка видалення", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadPrices();
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void loadPrices() {
        try {
            List<Price> prices = priceService.getAllPrices();
            priceList.setAll(prices);
            statusLabel.setText("Завантажено цін: " + prices.size());
        } catch (Exception e) {
            AlertUtil.showError("Помилка завантаження", e.getMessage());
            statusLabel.setText("Помилка завантаження даних.");
        }
    }

    private void fillForm(Price price) {
        editingPriceId = price.getId();
        productIdField.setText(String.valueOf(price.getProductId()));
        amountField.setText(String.valueOf(price.getAmount()));
        retailPriceField.setText(String.valueOf(price.getRetailPrice()));
        validToField.setText(price.getValidTo() != null ? price.getValidTo().format(formatter) : "");
        statusLabel.setText("Режим: редагування ціни ID = " + price.getId());
    }

    private void clearForm() {
        editingPriceId = null;
        productIdField.clear();
        amountField.clear();
        retailPriceField.clear();
        validToField.clear();
        priceTable.getSelectionModel().clearSelection();
        statusLabel.setText("Режим: додавання нової ціни");
    }

    private void configureDateColumn(TableColumn<Price, LocalDateTime> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
    }
}

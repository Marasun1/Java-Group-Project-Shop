package com.store.controller;

import com.store.model.Quantity;
import com.store.service.QuantityService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контролер сторінки залишків.
 * Працює із записами кількості товару по локаціях.
 */
public class QuantitiesController {

    @FXML private TableView<Quantity> quantityTable;
    @FXML private TableColumn<Quantity, Long> idColumn;
    @FXML private TableColumn<Quantity, Long> productIdColumn;
    @FXML private TableColumn<Quantity, String> locationColumn;
    @FXML private TableColumn<Quantity, Long> qtyColumn;
    @FXML private TableColumn<Quantity, LocalDateTime> lastUpdatedColumn;
    @FXML private TextField productIdField;
    @FXML private TextField locationField;
    @FXML private TextField qtyField;
    @FXML private Label statusLabel;

    private final QuantityService quantityService = new QuantityService();
    private final ObservableList<Quantity> quantityList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Long editingQuantityId = null;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));
        lastUpdatedColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));
        configureDateColumn(lastUpdatedColumn);

        quantityTable.setItems(quantityList);
        quantityTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, quantity) -> {
            if (quantity != null) {
                fillForm(quantity);
            }
        });

        loadQuantities();
    }

    @FXML
    private void handleSave() {
        try {
            Quantity quantity = new Quantity();
            quantity.setId(editingQuantityId);
            quantity.setProductId(ValidationUtil.positiveLong(productIdField.getText(), "Товар ID"));
            quantity.setLocation(ValidationUtil.required(locationField.getText(), "Локація"));
            quantity.setQty(ValidationUtil.nonNegativeLong(qtyField.getText(), "Кількість"));

            if (editingQuantityId == null) {
                quantityService.createQuantity(quantity);
                AlertUtil.showInfo("Успіх", "Залишок додано.");
            } else if (quantityService.updateQuantity(quantity)) {
                AlertUtil.showInfo("Успіх", "Залишок оновлено.");
            }

            loadQuantities();
            clearForm();
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Перевірка", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Помилка збереження", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Quantity selected = quantityTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Видалення", "Спочатку вибери залишок у таблиці.");
            return;
        }

        if (!AlertUtil.showConfirmation("Підтвердження", "Видалити вибраний залишок?")) {
            return;
        }

        try {
            quantityService.deleteQuantity(selected.getId());
            loadQuantities();
            clearForm();
            AlertUtil.showInfo("Успіх", "Залишок видалено.");
        } catch (Exception e) {
            AlertUtil.showError("Помилка видалення", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadQuantities();
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    /**
     * Завантажує всі залишки з бази даних у таблицю.
     */
    private void loadQuantities() {
        try {
            List<Quantity> quantities = quantityService.getAllQuantities();
            quantityList.setAll(quantities);
            statusLabel.setText("Завантажено залишків: " + quantities.size());
        } catch (Exception e) {
            AlertUtil.showError("Помилка завантаження", e.getMessage());
            statusLabel.setText("Помилка завантаження даних.");
        }
    }

    /**
     * Підставляє дані вибраного залишку у форму для редагування.
     *
     * @param quantity вибраний запис залишку
     */
    private void fillForm(Quantity quantity) {
        editingQuantityId = quantity.getId();
        productIdField.setText(String.valueOf(quantity.getProductId()));
        locationField.setText(quantity.getLocation());
        qtyField.setText(String.valueOf(quantity.getQty()));
        statusLabel.setText("Режим: редагування залишку ID = " + quantity.getId());
    }

    /**
     * Очищає форму та скидає режим редагування залишку.
     */
    private void clearForm() {
        editingQuantityId = null;
        productIdField.clear();
        locationField.clear();
        qtyField.clear();
        quantityTable.getSelectionModel().clearSelection();
        statusLabel.setText("Режим: додавання нового залишку");
    }

    /**
     * Налаштовує відображення значень дати й часу в таблиці.
     *
     * @param column колонка таблиці з датою
     */
    private void configureDateColumn(TableColumn<Quantity, LocalDateTime> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
    }
}

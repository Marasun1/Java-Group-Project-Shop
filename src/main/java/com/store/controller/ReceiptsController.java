package com.store.controller;

import com.store.model.Receipt;
import com.store.service.ReceiptService;
import com.store.util.AlertUtil;
import com.store.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptsController {

    @FXML private TableView<Receipt> receiptTable;
    @FXML private TableColumn<Receipt, Long> idColumn;
    @FXML private TableColumn<Receipt, Long> productIdColumn;
    @FXML private TableColumn<Receipt, Long> userIdColumn;
    @FXML private TableColumn<Receipt, Long> qtyReceivedColumn;
    @FXML private TableColumn<Receipt, java.math.BigDecimal> wholesalePriceColumn;
    @FXML private TableColumn<Receipt, LocalDateTime> receivedAtColumn;
    @FXML private TableColumn<Receipt, String> noteColumn;
    @FXML private TextField productIdField;
    @FXML private TextField userIdField;
    @FXML private TextField qtyReceivedField;
    @FXML private TextField locationField;
    @FXML private TextField wholesalePriceField;
    @FXML private TextArea noteArea;
    @FXML private Label statusLabel;

    private final ReceiptService receiptService = new ReceiptService();
    private final ObservableList<Receipt> receiptList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        qtyReceivedColumn.setCellValueFactory(new PropertyValueFactory<>("qtyReceived"));
        wholesalePriceColumn.setCellValueFactory(new PropertyValueFactory<>("wholesalePrice"));
        receivedAtColumn.setCellValueFactory(new PropertyValueFactory<>("receivedAt"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        configureDateColumn(receivedAtColumn);

        receiptTable.setItems(receiptList);
        loadReceipts();
    }

    @FXML
    private void handleSave() {
        try {
            Receipt receipt = new Receipt();
            receipt.setProductId(ValidationUtil.positiveLong(productIdField.getText(), "Товар ID"));
            receipt.setUserId(ValidationUtil.positiveLong(userIdField.getText(), "Користувач ID"));
            receipt.setQtyReceived(ValidationUtil.positiveLong(qtyReceivedField.getText(), "Кількість"));
            receipt.setWholesalePrice(ValidationUtil.nonNegativeDecimal(wholesalePriceField.getText(), "Гуртова ціна"));
            receipt.setNote(ValidationUtil.optional(noteArea.getText()));

            receiptService.createReceiptAndAddStock(
                    receipt,
                    ValidationUtil.required(locationField.getText(), "Локація")
            );
            loadReceipts();
            clearForm();
            AlertUtil.showInfo("Успіх", "Надходження додано, залишок оновлено.");
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Перевірка", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Помилка збереження", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Receipt selected = receiptTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Видалення", "Спочатку вибери надходження у таблиці.");
            return;
        }

        if (!AlertUtil.showConfirmation("Підтвердження", "Видалити вибране надходження?")) {
            return;
        }

        try {
            receiptService.deleteReceipt(selected.getId());
            loadReceipts();
            clearForm();
            AlertUtil.showInfo("Успіх", "Надходження видалено.");
        } catch (Exception e) {
            AlertUtil.showError("Помилка видалення", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadReceipts();
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void loadReceipts() {
        try {
            List<Receipt> receipts = receiptService.getAllReceipts();
            receiptList.setAll(receipts);
            statusLabel.setText("Завантажено надходжень: " + receipts.size());
        } catch (Exception e) {
            AlertUtil.showError("Помилка завантаження", e.getMessage());
            statusLabel.setText("Помилка завантаження даних.");
        }
    }

    private void clearForm() {
        productIdField.clear();
        userIdField.clear();
        qtyReceivedField.clear();
        locationField.clear();
        wholesalePriceField.clear();
        noteArea.clear();
        receiptTable.getSelectionModel().clearSelection();
        statusLabel.setText("Режим: додавання нового надходження");
    }

    private void configureDateColumn(TableColumn<Receipt, LocalDateTime> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
    }
}

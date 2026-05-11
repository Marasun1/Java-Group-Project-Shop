package com.store.controller;

import com.store.model.Receipt;
import com.store.service.ReceiptService;
import com.store.util.AlertUtil;
import com.store.util.TableColumnUtil;
import com.store.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контролер сторінки надходжень.
 * Створює записи надходжень і оновлює таблицю прийнятого товару.
 */
public class ReceiptsController {

    @FXML private TableView<Receipt> receiptTable;
    @FXML private TableColumn<Receipt, Long> idColumn;
    @FXML private TableColumn<Receipt, Long> productIdColumn;
    @FXML private TableColumn<Receipt, Long> userIdColumn;
    @FXML private TableColumn<Receipt, Long> roleIdColumn;
    @FXML private TableColumn<Receipt, String> supplierColumn;
    @FXML private TableColumn<Receipt, BigDecimal> qtyReceivedColumn;
    @FXML private TableColumn<Receipt, BigDecimal> costPriceColumn;
    @FXML private TableColumn<Receipt, LocalDate> expiresAtColumn;
    @FXML private TableColumn<Receipt, LocalDateTime> receivedAtColumn;
    @FXML private TableColumn<Receipt, String> noteColumn;
    @FXML private TextField productIdField;
    @FXML private TextField userIdField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField supplierField;
    @FXML private TextField invoiceNumberField;
    @FXML private TextField qtyReceivedField;
    @FXML private ComboBox<String> locationComboBox;
    @FXML private TextField costPriceField;
    @FXML private TextField expiresAtField;
    @FXML private TextArea noteArea;
    @FXML private Label statusLabel;

    private final ReceiptService receiptService = new ReceiptService();
    private final ObservableList<Receipt> receiptList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        roleIdColumn.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        qtyReceivedColumn.setCellValueFactory(new PropertyValueFactory<>("qtyReceived"));
        costPriceColumn.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        expiresAtColumn.setCellValueFactory(new PropertyValueFactory<>("expiresAt"));
        receivedAtColumn.setCellValueFactory(new PropertyValueFactory<>("receivedAt"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        TableColumnUtil.configureDateColumn(expiresAtColumn, dateFormatter);
        TableColumnUtil.configureDateTimeColumn(receivedAtColumn, formatter);

        roleComboBox.getItems().setAll("ADMIN", "MANAGER", "CLERK");
        roleComboBox.getSelectionModel().select("CLERK");
        locationComboBox.getItems().setAll("MAIN_STORAGE", "REFRIGERATOR", "FREEZER", "DRY_STORAGE", "QUARANTINE");

        receiptTable.setItems(receiptList);
        loadReceipts();
    }

    @FXML
    private void handleSave() {
        try {
            Receipt receipt = new Receipt();
            receipt.setProductId(ValidationUtil.positiveLong(productIdField.getText(), "Товар ID"));
            receipt.setUserId(ValidationUtil.positiveLong(userIdField.getText(), "Користувач ID"));
            receipt.setRoleId(resolveRoleId(ValidationUtil.required(roleComboBox.getValue(), "Роль")));
            receipt.setSupplier(ValidationUtil.required(supplierField.getText(), "Постачальник"));
            receipt.setInvoiceNumber(ValidationUtil.optional(invoiceNumberField.getText()));
            receipt.setQtyReceived(ValidationUtil.positiveDecimal(qtyReceivedField.getText(), "Кількість"));
            receipt.setCostPrice(ValidationUtil.nonNegativeDecimal(costPriceField.getText(), "Собівартість"));
            receipt.setExpiresAt(ValidationUtil.optionalDate(expiresAtField.getText(), "Придатний до", dateFormatter));
            receipt.setNote(ValidationUtil.optional(noteArea.getText()));

            receiptService.createReceiptAndAddStock(
                    receipt,
                    ValidationUtil.required(locationComboBox.getValue(), "Локація")
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

    /**
     * Завантажує всі надходження з бази даних у таблицю.
     */
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

    /**
     * Очищає форму надходження та повертає значення полів за замовчуванням.
     */
    private void clearForm() {
        productIdField.clear();
        userIdField.clear();
        roleComboBox.getSelectionModel().select("CLERK");
        supplierField.clear();
        invoiceNumberField.clear();
        qtyReceivedField.clear();
        locationComboBox.getSelectionModel().clearSelection();
        costPriceField.clear();
        expiresAtField.clear();
        noteArea.clear();
        receiptTable.getSelectionModel().clearSelection();
        statusLabel.setText("Режим: додавання нового надходження");
    }

    /**
     * Перетворює назву ролі на ідентифікатор, який очікує таблиця {@code receipts}.
     * Метод спирається на стандартні seed-дані ролей зі схеми БД.
     *
     * @param roleName назва ролі
     * @return ідентифікатор ролі
     */
    private Long resolveRoleId(String roleName) {
        return switch (roleName) {
            case "ADMIN" -> 1L;
            case "MANAGER" -> 2L;
            case "CLERK" -> 3L;
            default -> throw new IllegalArgumentException("Невідома роль: " + roleName);
        };
    }
}

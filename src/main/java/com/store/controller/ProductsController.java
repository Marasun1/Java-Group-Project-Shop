package com.store.controller;

import com.store.model.Product;
import com.store.service.ProductService;
import com.store.util.AlertUtil;
import com.store.util.TableColumnUtil;
import com.store.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контролер сторінки товарів.
 * Керує завантаженням, пошуком, створенням, редагуванням і видаленням товарів.
 */
public class ProductsController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Long> idColumn;
    @FXML private TableColumn<Product, String> skuColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> unitColumn;
    @FXML private TableColumn<Product, String> descriptionColumn;
    @FXML private TableColumn<Product, LocalDateTime> createdAtColumn;
    @FXML private TableColumn<Product, LocalDateTime> updatedAtColumn;
    @FXML private TextField skuField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField categoryField;
    @FXML private TextField unitField;
    @FXML private TextField searchSkuField;
    @FXML private TextField searchNameField;
    @FXML private Label statusLabel;

    private final ProductService productService = new ProductService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Long editingProductId;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        TableColumnUtil.configureDateTimeColumn(createdAtColumn, formatter);
        TableColumnUtil.configureDateTimeColumn(updatedAtColumn, formatter);

        productTable.setItems(productList);
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selectedProduct) -> {
            if (selectedProduct != null) {
                fillForm(selectedProduct);
            }
        });

        loadProducts();
    }

    @FXML
    private void handleSave() {
        try {
            String sku = ValidationUtil.requiredSku(skuField.getText());
            String name = ValidationUtil.required(nameField.getText(), "Назва");
            String description = ValidationUtil.optional(descriptionArea.getText());
            String category = ValidationUtil.required(categoryField.getText(), "Категорія");
            String unit = ValidationUtil.required(unitField.getText(), "Одиниця виміру");

            if (editingProductId == null) {
                Product newProduct = new Product(sku, name, description, category, unit);
                productService.createProduct(newProduct);
                AlertUtil.showInfo("Успіх", "Товар успішно додано.");
            } else {
                Product updatedProduct = new Product();
                updatedProduct.setId(editingProductId);
                updatedProduct.setSku(sku);
                updatedProduct.setName(name);
                updatedProduct.setDescription(description);
                updatedProduct.setCategory(category);
                updatedProduct.setUnit(unit);

                boolean updated = productService.updateProduct(updatedProduct);
                if (updated) {
                    AlertUtil.showInfo("Успіх", "Товар успішно оновлено.");
                } else {
                    AlertUtil.showWarning("Увага", "Не вдалося оновити товар.");
                }
            }

            loadProducts();
            clearForm();
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Перевірка", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Помилка збереження", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            AlertUtil.showWarning("Видалення", "Спочатку вибери товар у таблиці.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Підтвердження",
                "Ти справді хочеш видалити товар: " + selectedProduct.getName() + "?"
        );
        if (!confirmed) {
            return;
        }

        try {
            boolean deleted = productService.deleteProduct(selectedProduct.getId());
            if (deleted) {
                AlertUtil.showInfo("Успіх", "Товар видалено.");
                loadProducts();
                clearForm();
            } else {
                AlertUtil.showWarning("Увага", "Не вдалося видалити товар.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Помилка видалення", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        clearSearchFields();
        loadProducts();
    }

    @FXML
    private void handleSearch() {
        String sku = searchSkuField.getText() != null ? searchSkuField.getText().trim() : "";
        String name = searchNameField.getText() != null ? searchNameField.getText().trim() : "";

        if (sku.isBlank() && name.isBlank()) {
            AlertUtil.showWarning("Пошук", "Введи SKU або Назву для пошуку.");
            return;
        }

        try {
            List<Product> products = productService.searchProducts(sku, name);
            productList.setAll(products);
            productTable.getSelectionModel().clearSelection();
            setStatus("Знайдено товарів: " + products.size());
        } catch (Exception e) {
            AlertUtil.showError("Помилка пошуку", e.getMessage());
            setStatus("Помилка пошуку даних.");
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void fillForm(Product product) {
        editingProductId = product.getId();
        skuField.setText(product.getSku());
        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());
        categoryField.setText(product.getCategory());
        unitField.setText(product.getUnit());
        setStatus("Режим: редагування товару ID = " + product.getId());
    }

    private void clearForm() {
        editingProductId = null;
        skuField.clear();
        nameField.clear();
        descriptionArea.clear();
        categoryField.clear();
        unitField.clear();
        productTable.getSelectionModel().clearSelection();
        setStatus("Режим: додавання нового товару");
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productList.setAll(products);
            setStatus("Завантажено товарів: " + products.size());
        } catch (Exception e) {
            AlertUtil.showError("Помилка завантаження", e.getMessage());
            setStatus("Помилка завантаження даних.");
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void clearSearchFields() {
        searchSkuField.clear();
        searchNameField.clear();
    }
}

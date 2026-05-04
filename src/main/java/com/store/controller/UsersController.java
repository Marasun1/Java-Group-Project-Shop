package com.store.controller;

import com.store.model.AppUser;
import com.store.service.UserService;
import com.store.util.AlertUtil;
import com.store.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
 * Контролер сторінки користувачів.
 * Керує записами користувачів, ролями та перевіркою введених даних.
 */
public class UsersController {

    @FXML private TableView<AppUser> userTable;
    @FXML private TableColumn<AppUser, Long> idColumn;
    @FXML private TableColumn<AppUser, String> roleNameColumn;
    @FXML private TableColumn<AppUser, String> emailColumn;
    @FXML private TableColumn<AppUser, String> fullNameColumn;
    @FXML private TableColumn<AppUser, Boolean> activeColumn;
    @FXML private TableColumn<AppUser, LocalDateTime> createdAtColumn;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private TextField passwordField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Label statusLabel;

    private final UserService userService = new UserService();
    private final ObservableList<AppUser> userList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Long editingUserId = null;

    @FXML
    private void initialize() {
        roleComboBox.getItems().setAll("ADMIN", "MANAGER", "CLERK");
        roleComboBox.getSelectionModel().select("CLERK");
        activeCheckBox.setSelected(true);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roleNameColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        configureDateColumn(createdAtColumn);

        userTable.setItems(userList);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, user) -> {
            if (user != null) {
                fillForm(user);
            }
        });

        try {
            userService.ensureDefaultRoles();
            loadUsers();
        } catch (Exception e) {
            AlertUtil.showError("Помилка підготовки ролей", e.getMessage());
            statusLabel.setText("Помилка підготовки ролей.");
        }
    }

    @FXML
    private void handleSave() {
        try {
            AppUser user = new AppUser();
            user.setId(editingUserId);
            user.setRoleName(ValidationUtil.required(roleComboBox.getValue(), "Роль"));
            user.setEmail(ValidationUtil.requiredEmail(emailField.getText()));
            user.setFullName(ValidationUtil.required(fullNameField.getText(), "ПІБ"));
            user.setPasswordHash(validatePassword(passwordField.getText()));
            user.setActive(activeCheckBox.isSelected());

            if (editingUserId == null) {
                userService.createUser(user);
                AlertUtil.showInfo("Успіх", "Користувача додано.");
            } else if (userService.updateUser(user)) {
                AlertUtil.showInfo("Успіх", "Користувача оновлено.");
            }

            loadUsers();
            clearForm();
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Перевірка", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Помилка збереження", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        AppUser selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Видалення", "Спочатку вибери користувача у таблиці.");
            return;
        }

        if (!AlertUtil.showConfirmation("Підтвердження", "Видалити користувача " + selected.getEmail() + "?")) {
            return;
        }

        try {
            userService.deleteUser(selected.getId());
            loadUsers();
            clearForm();
            AlertUtil.showInfo("Успіх", "Користувача видалено.");
        } catch (Exception e) {
            AlertUtil.showError("Помилка видалення", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    /**
     * Завантажує всіх користувачів з бази даних у таблицю.
     */
    private void loadUsers() {
        try {
            List<AppUser> users = userService.getAllUsers();
            userList.setAll(users);
            statusLabel.setText("Завантажено користувачів: " + users.size());
        } catch (Exception e) {
            AlertUtil.showError("Помилка завантаження", e.getMessage());
            statusLabel.setText("Помилка завантаження даних.");
        }
    }

    /**
     * Заповнює форму даними вибраного користувача для редагування.
     *
     * @param user вибраний користувач
     */
    private void fillForm(AppUser user) {
        editingUserId = user.getId();
        roleComboBox.getSelectionModel().select(user.getRoleName());
        emailField.setText(user.getEmail());
        fullNameField.setText(user.getFullName());
        passwordField.setText(user.getPasswordHash());
        activeCheckBox.setSelected(Boolean.TRUE.equals(user.getActive()));
        statusLabel.setText("Режим: редагування користувача ID = " + user.getId());
    }

    /**
     * Очищає форму користувача та повертає стандартні значення полів.
     */
    private void clearForm() {
        editingUserId = null;
        roleComboBox.getSelectionModel().select("CLERK");
        emailField.clear();
        fullNameField.clear();
        passwordField.clear();
        activeCheckBox.setSelected(true);
        userTable.getSelectionModel().clearSelection();
        statusLabel.setText("Режим: додавання нового користувача");
    }

    /**
     * Налаштовує відображення колонки з датою створення користувача.
     *
     * @param column колонка таблиці з датою
     */
    private void configureDateColumn(TableColumn<AppUser, LocalDateTime> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
    }

    /**
     * Перевіряє коректність пароля перед збереженням користувача.
     *
     * @param value введене значення пароля
     * @return валідований пароль
     */
    private String validatePassword(String value) {
        String password = ValidationUtil.required(value, "Пароль");
        if (password.length() < 4) {
            throw new IllegalArgumentException("Пароль має містити щонайменше 4 символи.");
        }
        return password;
    }
}

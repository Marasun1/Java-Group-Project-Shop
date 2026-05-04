package com.store.service;

import com.store.model.AppUser;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервіс для CRUD-операцій із користувачами застосунку та їхніми ролями.
 */
public class UserService {

    private static final String[] DEFAULT_ROLES = {"ADMIN", "MANAGER", "CLERK"};

    private static final String SELECT_ALL_SQL = """
            SELECT u.id, u.role_id, r.name AS role_name, u.email, u.full_name,
                   u.password_hash, u.is_active, u.created_at
            FROM users u
            JOIN roles r ON r.id = u.role_id
            ORDER BY u.id
            """;

    private static final String INSERT_ROLE_SQL = """
            INSERT INTO roles (name, description)
            VALUES (?::role_type, ?)
            ON CONFLICT (name) DO NOTHING
            """;

    private static final String SELECT_ROLE_ID_SQL = """
            SELECT id
            FROM roles
            WHERE name = ?::role_type
            """;

    private static final String INSERT_SQL = """
            INSERT INTO users (role_id, email, full_name, password_hash, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            RETURNING id, created_at
            """;

    private static final String UPDATE_SQL = """
            UPDATE users
            SET role_id = ?, email = ?, full_name = ?, password_hash = ?, is_active = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM users
            WHERE id = ?
            """;

    /**
     * Гарантує наявність стандартних системних ролей у базі даних.
     */
    public void ensureDefaultRoles() {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ROLE_SQL)) {

            for (String role : DEFAULT_ROLES) {
                statement.setString(1, role);
                statement.setString(2, role + " role");
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося підготувати ролі користувачів.", e);
        }
    }

    /**
     * Завантажує всіх користувачів разом із назвами їхніх ролей.
     *
     * @return список користувачів
     */
    public List<AppUser> getAllUsers() {
        List<AppUser> users = new ArrayList<>();

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }

            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося отримати користувачів.", e);
        }
    }

    /**
     * Створює нового користувача застосунку.
     *
     * @param user дані користувача для збереження
     * @return збережений користувач із заповненими службовими полями
     */
    public AppUser createUser(AppUser user) {
        Long roleId = getRoleId(user.getRoleName());

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, roleId);
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getPasswordHash());
            statement.setBoolean(5, Boolean.TRUE.equals(user.getActive()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getLong("id"));
                    user.setRoleId(roleId);
                    user.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
                }
            }

            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося створити користувача. Перевір email на унікальність.", e);
        }
    }

    /**
     * Оновлює наявного користувача.
     *
     * @param user користувач з оновленими даними
     * @return {@code true}, якщо запис було оновлено
     */
    public boolean updateUser(AppUser user) {
        Long roleId = getRoleId(user.getRoleName());

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setLong(1, roleId);
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getPasswordHash());
            statement.setBoolean(5, Boolean.TRUE.equals(user.getActive()));
            statement.setLong(6, user.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося оновити користувача.", e);
        }
    }

    /**
     * Видаляє користувача за ідентифікатором.
     *
     * @param id ідентифікатор користувача
     * @return {@code true}, якщо користувача було видалено
     */
    public boolean deleteUser(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити користувача. Можливо, він використовується в надходженнях.", e);
        }
    }

    /**
     * Знаходить ідентифікатор ролі за її назвою.
     * Перед пошуком переконується, що стандартні ролі вже існують.
     *
     * @param roleName назва ролі
     * @return ідентифікатор ролі
     */
    private Long getRoleId(String roleName) {
        ensureDefaultRoles();

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ROLE_ID_SQL)) {

            statement.setString(1, roleName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
            }

            throw new RuntimeException("Роль не знайдено: " + roleName);
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося отримати роль користувача.", e);
        }
    }

    /**
     * Перетворює поточний рядок {@link ResultSet} у модель користувача.
     *
     * @param resultSet джерело даних з SQL-запиту
     * @return об'єкт користувача
     * @throws SQLException якщо не вдалося прочитати значення з результату запиту
     */
    private AppUser mapUser(ResultSet resultSet) throws SQLException {
        AppUser user = new AppUser();
        user.setId(resultSet.getLong("id"));
        user.setRoleId(resultSet.getLong("role_id"));
        user.setRoleName(resultSet.getString("role_name"));
        user.setEmail(resultSet.getString("email"));
        user.setFullName(resultSet.getString("full_name"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setActive(resultSet.getBoolean("is_active"));
        user.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        return user;
    }

    /**
     * Перетворює SQL-мітку часу у {@link LocalDateTime}.
     *
     * @param timestamp значення з бази даних
     * @return дата й час або {@code null}, якщо значення відсутнє
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}

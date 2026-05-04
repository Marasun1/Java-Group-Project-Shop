package com.store.service;

import com.store.model.Quantity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервіс для CRUD-операцій із залишками товарів.
 */
public class QuantityService {

    private static final String SELECT_ALL_SQL = """
            SELECT id, product_id, location, qty, last_updated
            FROM quantities
            ORDER BY id
            """;

    private static final String INSERT_SQL = """
            INSERT INTO quantities (product_id, location, qty, last_updated)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            RETURNING id, last_updated
            """;

    private static final String UPDATE_SQL = """
            UPDATE quantities
            SET product_id = ?, location = ?, qty = ?, last_updated = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM quantities
            WHERE id = ?
            """;

    /**
     * Завантажує всі записи залишків.
     *
     * @return список залишків товарів
     */
    public List<Quantity> getAllQuantities() {
        List<Quantity> quantities = new ArrayList<>();

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                quantities.add(mapQuantity(resultSet));
            }

            return quantities;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося отримати залишки.", e);
        }
    }

    /**
     * Створює новий запис залишку.
     *
     * @param quantity дані залишку для збереження
     * @return збережений запис залишку із заповненими службовими полями
     */
    public Quantity createQuantity(Quantity quantity) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, quantity.getProductId());
            statement.setString(2, quantity.getLocation());
            statement.setLong(3, quantity.getQty());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    quantity.setId(resultSet.getLong("id"));
                    quantity.setLastUpdated(toLocalDateTime(resultSet.getTimestamp("last_updated")));
                }
            }

            return quantity;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося створити залишок. Перевір ID товару.", e);
        }
    }

    /**
     * Оновлює наявний запис залишку.
     *
     * @param quantity залишок з оновленими даними
     * @return {@code true}, якщо запис було оновлено
     */
    public boolean updateQuantity(Quantity quantity) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setLong(1, quantity.getProductId());
            statement.setString(2, quantity.getLocation());
            statement.setLong(3, quantity.getQty());
            statement.setLong(4, quantity.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося оновити залишок.", e);
        }
    }

    /**
     * Видаляє запис залишку за ідентифікатором.
     *
     * @param id ідентифікатор залишку
     * @return {@code true}, якщо запис було видалено
     */
    public boolean deleteQuantity(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити залишок.", e);
        }
    }

    /**
     * Перетворює поточний рядок {@link ResultSet} у модель залишку.
     *
     * @param resultSet джерело даних з SQL-запиту
     * @return об'єкт залишку
     * @throws SQLException якщо не вдалося прочитати значення з результату запиту
     */
    private Quantity mapQuantity(ResultSet resultSet) throws SQLException {
        Quantity quantity = new Quantity();
        quantity.setId(resultSet.getLong("id"));
        quantity.setProductId(resultSet.getLong("product_id"));
        quantity.setLocation(resultSet.getString("location"));
        quantity.setQty(resultSet.getLong("qty"));
        quantity.setLastUpdated(toLocalDateTime(resultSet.getTimestamp("last_updated")));
        return quantity;
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

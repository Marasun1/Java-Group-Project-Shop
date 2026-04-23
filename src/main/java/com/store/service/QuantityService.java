package com.store.service;

import com.store.model.Quantity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public boolean deleteQuantity(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити залишок.", e);
        }
    }

    private Quantity mapQuantity(ResultSet resultSet) throws SQLException {
        Quantity quantity = new Quantity();
        quantity.setId(resultSet.getLong("id"));
        quantity.setProductId(resultSet.getLong("product_id"));
        quantity.setLocation(resultSet.getString("location"));
        quantity.setQty(resultSet.getLong("qty"));
        quantity.setLastUpdated(toLocalDateTime(resultSet.getTimestamp("last_updated")));
        return quantity;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}

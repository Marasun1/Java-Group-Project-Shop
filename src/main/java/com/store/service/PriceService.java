package com.store.service;

import com.store.model.Price;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PriceService {

    private static final String SELECT_ALL_SQL = """
            SELECT id, product_id, amount, retail_price, valid_from, valid_to
            FROM prices
            ORDER BY id
            """;

    private static final String INSERT_SQL = """
            INSERT INTO prices (product_id, amount, retail_price, valid_from, valid_to)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)
            RETURNING id, valid_from
            """;

    private static final String UPDATE_SQL = """
            UPDATE prices
            SET product_id = ?, amount = ?, retail_price = ?, valid_to = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM prices
            WHERE id = ?
            """;

    public List<Price> getAllPrices() {
        List<Price> prices = new ArrayList<>();

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                prices.add(mapPrice(resultSet));
            }

            return prices;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося отримати ціни.", e);
        }
    }

    public Price createPrice(Price price) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, price.getProductId());
            statement.setBigDecimal(2, price.getAmount());
            statement.setBigDecimal(3, price.getRetailPrice());
            setNullableTimestamp(statement, 4, price.getValidTo());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    price.setId(resultSet.getLong("id"));
                    price.setValidFrom(toLocalDateTime(resultSet.getTimestamp("valid_from")));
                }
            }

            return price;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося створити ціну. Перевір ID товару.", e);
        }
    }

    public boolean updatePrice(Price price) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setLong(1, price.getProductId());
            statement.setBigDecimal(2, price.getAmount());
            statement.setBigDecimal(3, price.getRetailPrice());
            setNullableTimestamp(statement, 4, price.getValidTo());
            statement.setLong(5, price.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося оновити ціну.", e);
        }
    }

    public boolean deletePrice(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити ціну.", e);
        }
    }

    private Price mapPrice(ResultSet resultSet) throws SQLException {
        Price price = new Price();
        price.setId(resultSet.getLong("id"));
        price.setProductId(resultSet.getLong("product_id"));
        price.setAmount(resultSet.getBigDecimal("amount"));
        price.setRetailPrice(resultSet.getBigDecimal("retail_price"));
        price.setValidFrom(toLocalDateTime(resultSet.getTimestamp("valid_from")));
        price.setValidTo(toLocalDateTime(resultSet.getTimestamp("valid_to")));
        return price;
    }

    private void setNullableTimestamp(PreparedStatement statement, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}

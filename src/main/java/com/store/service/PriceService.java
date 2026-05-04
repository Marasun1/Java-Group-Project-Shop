package com.store.service;

import com.store.model.Price;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервіс для CRUD-операцій із цінами товарів.
 */
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

    /**
     * Завантажує всі записи цін.
     *
     * @return список цін
     */
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

    /**
     * Створює новий запис ціни.
     *
     * @param price дані ціни для збереження
     * @return збережений запис ціни із заповненими службовими полями
     */
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

    /**
     * Оновлює наявний запис ціни.
     *
     * @param price ціна з оновленими даними
     * @return {@code true}, якщо запис було оновлено
     */
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

    /**
     * Видаляє запис ціни за ідентифікатором.
     *
     * @param id ідентифікатор ціни
     * @return {@code true}, якщо запис було видалено
     */
    public boolean deletePrice(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити ціну.", e);
        }
    }

    /**
     * Перетворює поточний рядок {@link ResultSet} у модель ціни.
     *
     * @param resultSet джерело даних з SQL-запиту
     * @return об'єкт ціни
     * @throws SQLException якщо не вдалося прочитати значення з результату запиту
     */
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

    /**
     * Записує в {@link PreparedStatement} дату завершення дії ціни або {@code null}.
     *
     * @param statement підготовлений SQL-запит
     * @param index позиція параметра
     * @param value дата завершення дії ціни
     * @throws SQLException якщо параметр не вдалося встановити
     */
    private void setNullableTimestamp(PreparedStatement statement, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(index, Timestamp.valueOf(value));
        }
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

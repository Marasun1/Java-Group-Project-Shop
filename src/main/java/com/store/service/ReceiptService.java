package com.store.service;

import com.store.model.Receipt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервіс для роботи з надходженнями товару.
 * Підтримує як просте створення надходження, так і транзакційне оновлення залишку.
 */
public class ReceiptService {

    private static final String SELECT_ALL_SQL = """
            SELECT id, product_id, user_id, qty_received, wholesale_price, received_at, note
            FROM receipts
            ORDER BY id
            """;

    private static final String INSERT_SQL = """
            INSERT INTO receipts (product_id, user_id, qty_received, wholesale_price, note)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, received_at
            """;

    private static final String SELECT_QUANTITY_FOR_UPDATE_SQL = """
            SELECT id
            FROM quantities
            WHERE product_id = ? AND location = ?
            ORDER BY id
            LIMIT 1
            FOR UPDATE
            """;

    private static final String UPDATE_QUANTITY_SQL = """
            UPDATE quantities
            SET qty = qty + ?, last_updated = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    private static final String INSERT_QUANTITY_SQL = """
            INSERT INTO quantities (product_id, location, qty, last_updated)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;

    private static final String DELETE_SQL = """
            DELETE FROM receipts
            WHERE id = ?
            """;

    /**
     * Завантажує всі записи надходжень.
     *
     * @return список надходжень
     */
    public List<Receipt> getAllReceipts() {
        List<Receipt> receipts = new ArrayList<>();

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                receipts.add(mapReceipt(resultSet));
            }

            return receipts;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося отримати надходження.", e);
        }
    }

    /**
     * Створює новий запис надходження без зміни залишків.
     *
     * @param receipt дані надходження для збереження
     * @return збережене надходження із заповненими службовими полями
     */
    public Receipt createReceipt(Receipt receipt) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, receipt.getProductId());
            statement.setLong(2, receipt.getUserId());
            statement.setLong(3, receipt.getQtyReceived());
            statement.setBigDecimal(4, receipt.getWholesalePrice());
            statement.setString(5, receipt.getNote());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    receipt.setId(resultSet.getLong("id"));
                    receipt.setReceivedAt(toLocalDateTime(resultSet.getTimestamp("received_at")));
                }
            }

            return receipt;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося створити надходження. Перевір ID товару та користувача.", e);
        }
    }

    /**
     * Створює надходження та оновлює залишок в межах однієї транзакції.
     * Якщо запис залишку для цього товару й локації вже існує,
     * його кількість збільшується; інакше створюється новий рядок.
     *
     * @param receipt дані надходження для збереження
     * @param location локація, куди надходить товар
     * @return збережене надходження із заповненими службовими полями
     */
    public Receipt createReceiptAndAddStock(Receipt receipt, String location) {
        try (Connection connection = DatabaseService.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Receipt savedReceipt = insertReceipt(connection, receipt);
                addQuantity(connection, savedReceipt.getProductId(), location, savedReceipt.getQtyReceived());

                connection.commit();
                return savedReceipt;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося створити надходження і оновити залишок. Перевір ID товару, користувача та локацію.", e);
        }
    }

    /**
     * Видаляє надходження за ідентифікатором.
     *
     * @param id ідентифікатор надходження
     * @return {@code true}, якщо надходження було видалено
     */
    public boolean deleteReceipt(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити надходження.", e);
        }
    }

    /**
     * Перетворює поточний рядок {@link ResultSet} у модель надходження.
     *
     * @param resultSet джерело даних з SQL-запиту
     * @return об'єкт надходження
     * @throws SQLException якщо не вдалося прочитати значення з результату запиту
     */
    private Receipt mapReceipt(ResultSet resultSet) throws SQLException {
        Receipt receipt = new Receipt();
        receipt.setId(resultSet.getLong("id"));
        receipt.setProductId(resultSet.getLong("product_id"));
        receipt.setUserId(resultSet.getLong("user_id"));
        receipt.setQtyReceived(resultSet.getLong("qty_received"));
        receipt.setWholesalePrice(resultSet.getBigDecimal("wholesale_price"));
        receipt.setReceivedAt(toLocalDateTime(resultSet.getTimestamp("received_at")));
        receipt.setNote(resultSet.getString("note"));
        return receipt;
    }

    /**
     * Виконує вставку надходження в межах уже відкритої транзакції.
     *
     * @param connection відкрите підключення до бази даних
     * @param receipt дані надходження
     * @return збережене надходження із заповненими службовими полями
     * @throws SQLException якщо вставка не вдалася
     */
    private Receipt insertReceipt(Connection connection, Receipt receipt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, receipt.getProductId());
            statement.setLong(2, receipt.getUserId());
            statement.setLong(3, receipt.getQtyReceived());
            statement.setBigDecimal(4, receipt.getWholesalePrice());
            statement.setString(5, receipt.getNote());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    receipt.setId(resultSet.getLong("id"));
                    receipt.setReceivedAt(toLocalDateTime(resultSet.getTimestamp("received_at")));
                }
            }
        }

        return receipt;
    }

    /**
     * Додає кількість товару до залишку в заданій локації.
     * Якщо запис уже існує, кількість збільшується; якщо ні - створюється новий.
     *
     * @param connection відкрите підключення до бази даних
     * @param productId ідентифікатор товару
     * @param location локація зберігання
     * @param qty кількість для додавання
     * @throws SQLException якщо операцію не вдалося виконати
     */
    private void addQuantity(Connection connection, Long productId, String location, Long qty) throws SQLException {
        Long quantityId = null;

        try (PreparedStatement statement = connection.prepareStatement(SELECT_QUANTITY_FOR_UPDATE_SQL)) {
            statement.setLong(1, productId);
            statement.setString(2, location);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    quantityId = resultSet.getLong("id");
                }
            }
        }

        if (quantityId == null) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT_QUANTITY_SQL)) {
                statement.setLong(1, productId);
                statement.setString(2, location);
                statement.setLong(3, qty);
                statement.executeUpdate();
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_QUANTITY_SQL)) {
                statement.setLong(1, qty);
                statement.setLong(2, quantityId);
                statement.executeUpdate();
            }
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

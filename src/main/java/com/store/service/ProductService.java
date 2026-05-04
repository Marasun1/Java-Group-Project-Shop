package com.store.service;

import com.store.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервіс для CRUD-операцій і пошуку товарів.
 */
public class ProductService {

    private static final String SELECT_ALL_SQL = """
            SELECT id, sku, name, description, created_at, updated_at
            FROM products
            ORDER BY id
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT id, sku, name, description, created_at, updated_at
            FROM products
            WHERE id = ?
            """;

    private static final String SEARCH_SQL = """
            SELECT id, sku, name, description, created_at, updated_at
            FROM products
            WHERE (? IS NULL OR sku ILIKE ?)
              AND (? IS NULL OR name ILIKE ?)
            ORDER BY id
            """;

    private static final String INSERT_SQL = """
            INSERT INTO products (sku, name, description, created_at, updated_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            RETURNING id, created_at, updated_at
            """;

    private static final String UPDATE_SQL = """
            UPDATE products
            SET sku = ?, name = ?, description = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM products
            WHERE id = ?
            """;

    /**
     * Завантажує всі товари, відсортовані за ідентифікатором.
     *
     * @return список усіх товарів
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }

            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося отримати список товарів.", e);
        }
    }

    /**
     * Шукає товар за його ідентифікатором.
     *
     * @param id ідентифікатор товару
     * @return {@code Optional} з товаром або порожнє значення, якщо товар не знайдено
     */
    public Optional<Product> getProductById(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapProduct(resultSet));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося отримати товар за ID.", e);
        }
    }

    /**
     * Виконує пошук товарів за SKU і/або назвою з частковим збігом.
     *
     * @param sku необов'язковий фрагмент SKU
     * @param name необов'язковий фрагмент назви
     * @return список знайдених товарів
     */
    public List<Product> searchProducts(String sku, String name) {
        List<Product> products = new ArrayList<>();
        String skuFilter = normalizeFilter(sku);
        String nameFilter = normalizeFilter(name);

        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {

            statement.setString(1, skuFilter);
            statement.setString(2, skuFilter);
            statement.setString(3, nameFilter);
            statement.setString(4, nameFilter);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }

            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося виконати пошук товарів.", e);
        }
    }

    /**
     * Створює новий запис товару.
     *
     * @param product дані товару для збереження
     * @return збережений товар із заповненими службовими полями
     */
    public Product createProduct(Product product) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, product.getSku());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    product.setId(resultSet.getLong("id"));
                    product.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
                    product.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
                }
            }

            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося створити товар. Перевір SKU на унікальність.", e);
        }
    }

    /**
     * Оновлює наявний товар.
     *
     * @param product товар з оновленими даними
     * @return {@code true}, якщо було оновлено хоча б один рядок
     */
    public boolean updateProduct(Product product) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, product.getSku());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setLong(4, product.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося оновити товар.", e);
        }
    }

    /**
     * Видаляє товар за ідентифікатором.
     *
     * @param id ідентифікатор товару
     * @return {@code true}, якщо товар було видалено
     */
    public boolean deleteProduct(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити товар.", e);
        }
    }

    /**
     * Перетворює поточний рядок {@link ResultSet} у модель товару.
     *
     * @param resultSet джерело даних з SQL-запиту
     * @return об'єкт товару
     * @throws SQLException якщо не вдалося прочитати значення з результату запиту
     */
    private Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getLong("id"));
        product.setSku(resultSet.getString("sku"));
        product.setName(resultSet.getString("name"));
        product.setDescription(resultSet.getString("description"));
        product.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        product.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return product;
    }

    /**
     * Нормалізує значення для пошуку та додає шаблон для SQL {@code ILIKE}.
     *
     * @param value сире значення з поля пошуку
     * @return шаблон пошуку або {@code null}, якщо поле порожнє
     */
    private String normalizeFilter(String value) {
        String trimmed = value != null ? value.trim() : "";
        return trimmed.isBlank() ? null : "%" + trimmed + "%";
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

package com.store.service;

import com.store.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public boolean deleteProduct(Long id) {
        try (Connection connection = DatabaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося видалити товар.", e);
        }
    }

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

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
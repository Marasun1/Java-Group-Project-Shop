package com.store.util;

import com.store.service.DatabaseService;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Допоміжна точка входу для ручної перевірки підключення до бази даних.
 */
public class ConnectionTest {

    /**
     * Підключається до бази даних і виводить результат у консоль.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        try (Connection connection = DatabaseService.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Connection to PostgreSQL was successful.");
            } else {
                System.out.println("Connection failed.");
            }
        } catch (SQLException e) {
            System.out.println("Database connection error:");
            e.printStackTrace();
        }
    }
}

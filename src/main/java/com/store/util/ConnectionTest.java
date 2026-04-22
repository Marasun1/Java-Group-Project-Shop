package com.store.util;

import com.store.service.DatabaseService;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTest {

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
package com.store.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseService {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream =
                     DatabaseService.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {

            if (inputStream == null) {
                throw new RuntimeException("Не знайдено файл " + PROPERTIES_FILE);
            }

            properties.load(inputStream);

        } catch (IOException e) {
            throw new RuntimeException("Не вдалося завантажити налаштування БД.", e);
        }
    }

    private DatabaseService() {
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }
}
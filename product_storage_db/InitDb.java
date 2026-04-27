import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InitDb {
    public static void main(String[] args) {
        String dbName = "store_db";
        String user = "postgres";
        String password = "0000";
        String createDbUrl = "jdbc:postgresql://localhost:5432/postgres";
        String dbUrl = "jdbc:postgresql://localhost:5432/" + dbName;

        try {
            // Register driver
            Class.forName("org.postgresql.Driver");

            // 1. Create DB if it doesn't exist
            try (Connection conn = DriverManager.getConnection(createDbUrl, user, password);
                 Statement stmt = conn.createStatement()) {
                System.out.println("Checking if DB " + dbName + " exists...");
                // Just try to create it. We catch exception if it already exists.
                try {
                    stmt.executeUpdate("CREATE DATABASE " + dbName);
                    System.out.println("Database created.");
                } catch (Exception e) {
                    System.out.println("Database probably already exists.");
                }
            }

            // 2. Connect to the actual db and run the script
            try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
                 Statement stmt = conn.createStatement()) {
                System.out.println("Connected to " + dbName + ". Applying schema...");
                String sql = new String(Files.readAllBytes(Paths.get("schema.sql")));
                stmt.execute(sql);
                System.out.println("Schema applied successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.ComponentandDatabase.Database_Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String SERVER_NAME = "";
    private static final String DATABASE_NAME = "QuanLyKho";
    private static final String URL = String.format(
        "jdbc:sqlserver://%s:1433;databaseName=%s;encrypt=false;trustServerCertificate=true",
        SERVER_NAME, DATABASE_NAME);
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static Connection conn = null;

    static {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("✅ JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            String error = "Error loading JDBC driver. Make sure mssql-jdbc jar is in lib folder";
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }

    // Kết nối CSDL với xử lý lỗi nâng cao - Tạo connection mới mỗi lần
    public static Connection connect() {
        try {
            System.out.println("Attempting DB connection to (instance lookup): " + URL + " with user=" + USER);
            try {
                Connection newConn = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established via instance lookup.");
                return newConn;
            } catch (SQLException exInstance) {
                System.err.println("Instance lookup failed: " + exInstance.getMessage());
                // Fallback: try default port 1433 on the server host
                String fallbackUrl = "jdbc:sqlserver://" + SERVER_NAME + ":1433;databaseName=" + DATABASE_NAME + ";encrypt=false;loginTimeout=30";
                System.out.println("Attempting fallback DB connection to: " + fallbackUrl + " with user=" + USER);
                try {
                    Connection newConn = DriverManager.getConnection(fallbackUrl, USER, PASSWORD);
                    System.out.println("Database connection established via fallback (host:1433).");
                    return newConn;
                } catch (SQLException exPort) {
                    System.err.println("Fallback connection failed: " + exPort.getMessage());
                    // rethrow last exception to be visible to callers/logs
                    throw exPort;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Phương thức đóng kết nối có xử lý lỗi tốt hơn
    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("🔌 Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("⚠ Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Kiểm tra trạng thái kết nối
    public static boolean isConnectionValid() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("⚠ Connection validation failed: " + e.getMessage());
            return false;
        }
    }

    public static void testConnection() {
        try {
            Connection conn = DatabaseConnection.connect();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connected successfully!");
                System.out.println("Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database Version: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("Connected Database: " + conn.getCatalog());
                closeConnection();
            }
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

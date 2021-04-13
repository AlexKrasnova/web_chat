package ru.geekbrains.alexkrasnova.webchat.server.user.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private Connection connection;
    private Statement stmt;

    public Statement getStmt() {
        return stmt;
    }

    public Connection getConnection() {
        return connection;
    }

    public DatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:web_chat.db");
            this.stmt = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Невозможно подключиться к базе данных");
        }
    }

    public void close() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}

package ru.geekbrains.alexkrasnova.webchat.server.user.service;

import ru.geekbrains.alexkrasnova.webchat.server.exception.AuthenticationException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.UsernameAlreadyExistsException;
import ru.geekbrains.alexkrasnova.webchat.server.user.User;

import java.sql.*;

public class DatabaseUserService implements UserService {

    private Connection connection;
    private Statement stmt;

    public DatabaseUserService() {
        try {
            connect();
            dropAndCreateTable();
            fillTable();
            //readExample();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User checkCredentialsAndGetUser(String login, String password) throws AuthenticationException {
        try (ResultSet rs = stmt.executeQuery("select login, password, username from users where login = '" + login + "';")) {
            if (rs.next()) {
                if (rs.getString(2).equals(password)) {
                    return new User(rs.getString(1), rs.getString(2), rs.getString(3));
                }
                throw new AuthenticationException("Неверный пароль");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new AuthenticationException("Логин не найден");
    }

    @Override
    public void addUser(User user) throws UsernameAlreadyExistsException {
        try {
            if (!isUsernameBusy(user.getUsername())) {
                stmt.executeUpdate(String.format("insert into users (login, password, username) values ('%s', '%s', '%s');", user.getLogin(), user.getPassword(), user.getUsername()));
                return;
            }
            throw new UsernameAlreadyExistsException();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isUsernameBusy(String username) {
        try (ResultSet rs = stmt.executeQuery("select * from users where username = '" + username + "';")) {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User getUserByLogin(String login) {
        try (ResultSet rs = stmt.executeQuery("select login, password, username from users where login = '" + login + "';")) {
            if (rs.next()) {
                return new User(rs.getString(1), rs.getString(2), rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //todo: Разобраться, какое исключение здесь выбрасывать
        throw new RuntimeException("Пользователь не найден");
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server/web_chat.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Невозможно подключиться к БД");
        }
    }

    @Override
    public void disconnect() {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public User changeUsernameAndGetUser(String login, String newUsername) throws UsernameAlreadyExistsException{
        try {
            stmt.executeUpdate("update users set username = '" + newUsername + "' where login = '" + login + "';");
            return getUserByLogin(login);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new UsernameAlreadyExistsException();
    }

    private void dropAndCreateTable() throws SQLException {
        stmt.executeUpdate("drop table if exists users;");
        stmt.executeUpdate("create table if not exists users (\n" +
                "    id    INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    login  TEXT,\n" +
                "    password TEXT,\n" +
                "    username TEXT\n" +
                ");;");
    }

    private void fillTable() throws SQLException {
        connection.setAutoCommit(false);
        addUser(new User("sally", "2", "Sally"));
        addUser(new User("bob", "1", "Bobby"));
        addUser(new User("max", "1", "Maxim"));
        addUser(new User("alex", "1", "Sasha"));
        addUser(new User("david", "1", "David"));
        /*
        stmt.executeUpdate(String.format("insert into users (login, password, username) values ('%s', '%s', '%s');", "bob", "1", "Bobby"));
        stmt.executeUpdate(String.format("insert into users (login, password, username) values ('%s', '%s', '%s');", "max", "1", "Maxim"));
        stmt.executeUpdate(String.format("insert into users (login, password, username) values ('%s', '%s', '%s');", "alex", "1", "Sasha"));
        stmt.executeUpdate(String.format("insert into users (login, password, username) values ('%s', '%s', '%s');", "david", "1", "David"));
        */
        connection.commit();
    }

    private void readExample() throws SQLException {
        try (ResultSet rs = stmt.executeQuery("select * from users;")) {
            while (rs.next()) {
                System.out.println(rs.getInt(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4));
            }
        }
    }
}

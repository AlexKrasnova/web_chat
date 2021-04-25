package ru.geekbrains.alexkrasnova.webchat.server.user.service;

import org.apache.logging.log4j.Level;
import ru.geekbrains.alexkrasnova.webchat.server.Server;
import ru.geekbrains.alexkrasnova.webchat.server.exception.AuthenticationException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.UsernameAlreadyExistsException;
import ru.geekbrains.alexkrasnova.webchat.server.user.User;

import java.sql.*;

public class DatabaseUserService implements UserService {

    private DatabaseConnection databaseConnection;

    @Override
    public void init() {
        databaseConnection = new DatabaseConnection();
        try {
            dropAndCreateTable();
            fillTable();
        } catch (SQLException e) {
            Server.LOGGER.throwing(Level.ERROR, e);
        }

    }

    @Override
    public User checkCredentialsAndGetUser(String login, String password) throws AuthenticationException {
        String query = "select login, password, username from users where login = '" + login + "';";
        try (ResultSet rs = databaseConnection.getStmt().executeQuery(query)) {
            if (rs.next()) {
                if (rs.getString(2).equals(password)) {
                    return new User(rs.getString(1), rs.getString(2), rs.getString(3));
                }
                throw new AuthenticationException("Неверный пароль");
            }
        } catch (SQLException e) {
            Server.LOGGER.throwing(Level.ERROR, e);
        }
        throw new AuthenticationException("Логин не найден");
    }

    @Override
    public void addUser(User user) throws UsernameAlreadyExistsException {
        try {
            if (!isUsernameBusy(user.getUsername())) {
                String query = String.format("insert into users (login, password, username) values ('%s', '%s', '%s');", user.getLogin(), user.getPassword(), user.getUsername());
                databaseConnection.getStmt().executeUpdate(query);
                return;
            }
            throw new UsernameAlreadyExistsException();

        } catch (SQLException e) {
            Server.LOGGER.throwing(Level.ERROR, e);
        }
    }

    private boolean isUsernameBusy(String username) {
        String query = "select id from users where username = '" + username + "';";
        try (ResultSet rs = databaseConnection.getStmt().executeQuery(query)) {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            Server.LOGGER.throwing(Level.ERROR, e);
        }
        return false;
    }

    private User getUserByLogin(String login) {
        String query = "select login, password, username from users where login = '" + login + "';";
        try (ResultSet rs = databaseConnection.getStmt().executeQuery(query)) {
            if (rs.next()) {
                return new User(rs.getString(1), rs.getString(2), rs.getString(3));
            }
        } catch (SQLException e) {
            Server.LOGGER.throwing(Level.ERROR, e);
        }
        //todo: Разобраться, какое исключение здесь выбрасывать
        throw new RuntimeException("Пользователь не найден");
    }

    @Override
    public User changeUsernameAndGetUser(String login, String newUsername) throws UsernameAlreadyExistsException {
        String query = "update users set username = '" + newUsername + "' where login = '" + login + "';";
        try {
            databaseConnection.getStmt().executeUpdate(query);
            return getUserByLogin(login);
        } catch (SQLException e) {
            Server.LOGGER.throwing(Level.ERROR, e);
        }
        throw new UsernameAlreadyExistsException();
    }


    private void dropAndCreateTable() throws SQLException {
        databaseConnection.getStmt().executeUpdate("drop table if exists users;");
        databaseConnection.getStmt().executeUpdate("create table if not exists users (\n" +
                "    id    INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    login  TEXT,\n" +
                "    password TEXT,\n" +
                "    username TEXT\n" +
                ");;");
    }

    private void fillTable() throws SQLException {
        databaseConnection.getConnection().setAutoCommit(false);
        addUser(new User("sally", "2", "Sally"));
        addUser(new User("bob", "1", "Bobby"));
        addUser(new User("max", "1", "Maxim"));
        addUser(new User("alex", "1", "Sasha"));
        addUser(new User("david", "1", "David"));
        databaseConnection.getConnection().commit();
    }

    @Override
    public void shutdown() {
        databaseConnection.close();
    }
}

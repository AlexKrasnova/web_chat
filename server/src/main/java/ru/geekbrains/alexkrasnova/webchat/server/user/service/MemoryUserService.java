package ru.geekbrains.alexkrasnova.webchat.server.user.service;


import ru.geekbrains.alexkrasnova.webchat.server.user.User;
import ru.geekbrains.alexkrasnova.webchat.server.exception.UsernameAlreadyExistsException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.AuthenticationException;

import java.util.HashMap;
import java.util.Map;


public class MemoryUserService implements UserService {

    Map<String, User> users;

    public MemoryUserService() {
        users = new HashMap<>();
        addUser(new User("bob@gmail.com", "bob1997", "Bob"));
        addUser(new User("john@gmail.com", "john1990", "John"));
        addUser(new User("jack@gmail.com", "jack1980", "Jack"));
        addUser(new User("max@gmail.com", "1234", "Max"));
        addUser(new User("ann@gmail.com", "4321", "Ann"));
        addUser(new User("cathrine@gmail.com", "1111", "Cat"));
        addUser(new User("mary@gmail.com", "222", "Mary"));
        addUser(new User("vasya@yandex.ru", "1", "Vasya"));
        addUser(new User("gosha@yandex.ru", "22222", "Gosha"));
        addUser(new User("sasha@mail.ru", "333", "Sasha"));
        addUser(new User("yana@gmail.com", "666", "Yana"));
        addUser(new User("vika@mail.ru", "666", "Vika"));
        addUser(new User("oleg@mail.ru", "1234", "Oleg"));

    }

    @Override
    public User checkCredentialsAndGetUser(String login, String password) throws AuthenticationException {
        if (users.containsKey(login)) {
            if (users.get(login).getPassword().equals(password)) {
                return users.get(login);
            }
            throw new AuthenticationException("Неверный пароль");
        }
        throw new AuthenticationException("Логин не найден");
    }

    @Override
    public void addUser(User user) {
        if (!isUsernameBusy(user.getUsername())) {
            users.put(user.getLogin(), user);
            return;
        }
        throw new UsernameAlreadyExistsException();
    }

    private boolean isUsernameBusy(String username) {
        for (Map.Entry<String, User> userEntry : users.entrySet()) {
            if (userEntry.getValue().getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private User getUserByLogin(String login) {
        return users.get(login);
    }

    @Override
    public User changeUsernameAndGetUser(String login, String newUsername) throws UsernameAlreadyExistsException {
        if (!isUsernameBusy(newUsername)) {
            users.get(login).setUsername(newUsername);
            return users.get(login);
        }
        throw new UsernameAlreadyExistsException();
    }

}

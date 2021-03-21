package ru.geekbrains.alexkrasnova.webchat.server;

import ru.geekbrains.alexkrasnova.webchat.server.exception.UsernameAlreadyExistsException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.loginFailedException.InvalidPasswordException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.loginFailedException.LoginFailedException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.loginFailedException.LoginNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class UserService {

    Map<String, User> users;

    public UserService() {
        users = new HashMap<>();
    }

    public String checkCredentialsAndGetUsername(String login, String password) throws LoginFailedException {
        if(users.containsKey(login)) {
            if (users.get(login).getPassword().equals(password)) {
                return users.get(login).getUsername();
            }
            throw new InvalidPasswordException();
        }
        throw new LoginNotFoundException();
    }

    public void addUser(User user) {
        if(!isUsernameBusy(user.getUsername())) {
            users.put(user.getLogin(), user);
            return;
        }
        throw new UsernameAlreadyExistsException();
    }

    public boolean isUsernameBusy(String username) {
        for (Map.Entry<String, User> userEntry : users.entrySet()) {
            if(userEntry.getValue().getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public User getUserByLogin(String login) {
        return users.get(login);
    }

}

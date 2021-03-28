package ru.geekbrains.alexkrasnova.webchat.server.user.service;

import ru.geekbrains.alexkrasnova.webchat.server.exception.AuthenticationException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.UsernameAlreadyExistsException;
import ru.geekbrains.alexkrasnova.webchat.server.user.User;


public interface UserService {

    User checkCredentialsAndGetUser(String login, String password) throws AuthenticationException;

    void addUser(User user) throws UsernameAlreadyExistsException;

    boolean isUsernameBusy(String username);

    User getUserByLogin(String login);

    void disconnect();

    User changeUsernameAndGetUser(String login, String newUsername) throws UsernameAlreadyExistsException;
}

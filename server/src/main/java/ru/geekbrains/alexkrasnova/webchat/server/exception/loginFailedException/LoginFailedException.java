package ru.geekbrains.alexkrasnova.webchat.server.exception.loginFailedException;

public class LoginFailedException extends RuntimeException{
    public LoginFailedException(String message) {
        super(message);
    }
}

package ru.geekbrains.alexkrasnova.webchat.server.exception;

public class LoginAlreadyExistsException extends RuntimeException {
    public LoginAlreadyExistsException() {
        super("Данный логин уже занят");
    }
}

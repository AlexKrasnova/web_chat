package ru.geekbrains.alexkrasnova.webchat.server.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() {
        super("Данное имя пользователя уже занято");
    }
}

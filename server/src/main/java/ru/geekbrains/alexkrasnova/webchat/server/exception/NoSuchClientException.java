package ru.geekbrains.alexkrasnova.webchat.server.exception;

public class NoSuchClientException extends RuntimeException {

    public NoSuchClientException () {
        super("Такого пользователя не существует");
    }
}

package ru.geekbrains.alexkrasnova.webchat.server.exception;

public class InvalidCommandMessageException extends RuntimeException {
    public InvalidCommandMessageException (String message) {
        super(message);
    }
}

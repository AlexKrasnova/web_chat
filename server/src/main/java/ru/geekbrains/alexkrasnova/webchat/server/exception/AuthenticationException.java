package ru.geekbrains.alexkrasnova.webchat.server.exception;

public class AuthenticationException extends RuntimeException{
    public AuthenticationException(String message) {
        super(message);
    }
}

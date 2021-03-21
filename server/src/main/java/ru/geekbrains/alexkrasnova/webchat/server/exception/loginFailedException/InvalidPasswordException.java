package ru.geekbrains.alexkrasnova.webchat.server.exception.loginFailedException;

public class InvalidPasswordException extends LoginFailedException{
    public InvalidPasswordException () {
        super("Неверный пароль");
    }
}

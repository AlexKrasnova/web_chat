package ru.geekbrains.alexkrasnova.webchat.server.exception.loginFailedException;

public class LoginNotFoundException extends LoginFailedException{
    public LoginNotFoundException() {
        super("Неверный логин");
    }
}

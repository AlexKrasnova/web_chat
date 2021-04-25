package ru.geekbrains.alexkrasnova.webchat.client;

@FunctionalInterface
public interface Callback {
    void callback(Object... args);
}

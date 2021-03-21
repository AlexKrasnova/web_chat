package ru.geekbrains.alexkrasnova.webchat.server;

import ru.geekbrains.alexkrasnova.webchat.server.exception.NoSuchClientException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    List<ClientHandler> clients;
    UserService userService;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
        userService = initializeUserService();
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                System.out.println("Ожидание подключения нового клиента...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вошел в чат");
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вышел из чата");
        broadcastClientsList();
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public void sendMessage(ClientHandler addressant, String addresseeUsername, String message) throws IOException, NoSuchClientException {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(addresseeUsername)) {
                clientHandler.sendMessage(addressant.getUsername() + " для " + addresseeUsername + ": " + message);
                addressant.sendMessage(addressant.getUsername() + " для " + addresseeUsername + ": " + message);
                return;
            }
        }
        throw new NoSuchClientException();
    }

    public synchronized void broadcastClientsList() {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        for (ClientHandler c : clients) {
            stringBuilder.append(c.getUsername()).append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        String clientsList = stringBuilder.toString();
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(clientsList);
        }
    }

    public boolean isUserOnline(String username) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private UserService initializeUserService() {
        UserService userService = new UserService();
        userService.addUser(new User("bob@gmail.com", "bob1997", "Bob"));
        userService.addUser(new User("john@gmail.com", "john1990", "John"));
        userService.addUser(new User("jack@gmail.com", "jack1980", "Jack"));
        userService.addUser(new User("max@gmail.com", "1234", "Max"));
        userService.addUser(new User("ann@gmail.com", "4321", "Ann"));
        userService.addUser(new User("cathrine@gmail.com", "1111", "Cat"));
        userService.addUser(new User("mary@gmail.com", "222", "Mary"));
        userService.addUser(new User("vasya@yandex.ru", "1", "Vasya"));
        userService.addUser(new User("gosha@yandex.ru", "22222", "Gosha"));
        userService.addUser(new User("sasha@mail.ru", "333", "Sasha"));
        userService.addUser(new User("yana@gmail.com", "666", "Yana"));
        userService.addUser(new User("vika@mail.ru", "666", "Vika"));
        userService.addUser(new User("oleg@mail.ru", "1234", "Oleg"));
        return userService;
    }

}

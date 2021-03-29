package ru.geekbrains.alexkrasnova.webchat.server;

import ru.geekbrains.alexkrasnova.webchat.server.exception.NoSuchClientException;
import ru.geekbrains.alexkrasnova.webchat.server.user.User;
import ru.geekbrains.alexkrasnova.webchat.server.user.service.DatabaseUserService;
import ru.geekbrains.alexkrasnova.webchat.server.user.service.MemoryUserService;
import ru.geekbrains.alexkrasnova.webchat.server.user.service.UserService;

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
        userService = new DatabaseUserService();
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
        } finally {
            userService.freeUpResources();
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

    public boolean isUserOnline(User user) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

}

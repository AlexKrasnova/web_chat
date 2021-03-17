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

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
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
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void broadcastMessage(String message) throws IOException {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public void sendMessage(ClientHandler addressant, String addresseeUsername, String message) throws IOException, NoSuchClientException {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(addresseeUsername)) {
                clientHandler.sendMessage(addressant.getUsername() + " to " + addresseeUsername + ": " + message);
                addressant.sendMessage(addressant.getUsername() + " to " + addresseeUsername + ": " + message);
                return;
            }
        }
        throw new NoSuchClientException();
    }

    public boolean isNickBusy(String username) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

}

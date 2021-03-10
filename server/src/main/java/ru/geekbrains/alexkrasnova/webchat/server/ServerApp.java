package ru.geekbrains.alexkrasnova.webchat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {

            System.out.println("Сервер запущен на порту " + serverSocket.getLocalPort() + ". Ожидаем подключения клиента...");
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Клиент подключился");

            int msgCount = 0;
            while (true) {
                String msg = in.readUTF();
                msgCount++;
                System.out.println(msg);
                if (msg.equals("/exit")) {
                    out.writeUTF(msg);
                    socket.close();
                    System.out.println("Клиент отключился");
                    break;
                }
                if (msg.equals("/stat")) {
                    out.writeUTF("Количество сообщений: " + msgCount);
                    msgCount--;
                } else {
                    out.writeUTF("ECHO: " + msg);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

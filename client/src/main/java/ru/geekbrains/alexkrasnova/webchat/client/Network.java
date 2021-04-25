package ru.geekbrains.alexkrasnova.webchat.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private final String COMMAND_MESSAGE_SYMBOL = "/";
    private final String LOGIN = "/login ";
    private final String LOGIN_FAILED = "/login_failed ";
    private final String LOGIN_OK = "/login_ok ";
    private final String CHANGE_ACCOUNT = "/change_account ";
    private final String EXIT = "/exit";

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread listeningThread;

    private Callback onAuthOkCallback;
    private Callback onAuthFailedCallback;
    private Callback onMessageReceivedCallback;
    private Callback onConnectCallback;
    private Callback onDisconnectCallback;

    public void setOnAuthOkCallback(Callback onAuthOkCallback) {
        this.onAuthOkCallback = onAuthOkCallback;
    }

    public void setOnAuthFailedCallback(Callback onAuthFailedCallback) {
        this.onAuthFailedCallback = onAuthFailedCallback;
    }

    public void setOnMessageReceivedCallback(Callback onMessageReceivedCallback) {
        this.onMessageReceivedCallback = onMessageReceivedCallback;
    }

    public void setOnConnectCallback(Callback onConnectCallback) {
        this.onConnectCallback = onConnectCallback;
    }

    public void setOnDisconnectCallback(Callback onDisconnectCallback) {
        this.onDisconnectCallback = onDisconnectCallback;
    }

    public void connect(int port) throws IOException {
        socket = new Socket("localhost", port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        if (onConnectCallback != null) {
            onConnectCallback.callback();
        }

        listeningThread = new Thread(() -> {

            try {
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith(LOGIN_OK)) {
                        if (onAuthOkCallback != null) {
                            onAuthOkCallback.callback(message);
                        }
                        break;
                    }
                    if (message.startsWith(LOGIN_FAILED)) {
                        String cause = in.readUTF();
                        if (onAuthFailedCallback != null) {
                            onAuthFailedCallback.callback(cause);
                        }
                    }
                }

                while (true) {
                    String message = in.readUTF();
                    if (message.equals(EXIT)) {
                        if (onDisconnectCallback != null) {
                            onDisconnectCallback.callback(message);
                            disconnect();
                            break;
                        }
                    }
                    if (onMessageReceivedCallback != null) {
                        onMessageReceivedCallback.callback(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        });
        listeningThread.start();
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
        if (message.equals(EXIT)) {
            try {
                listeningThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            disconnect();
            /*Alert alert = new Alert(Alert.AlertType.INFORMATION, "Процесс обмена сообщениями завершен", ButtonType.OK);
            alert.showAndWait();*/
        }
    }

    public void tryToLogin(String login, String password) throws IOException {
        sendMessage(LOGIN + login + " " + password);
    }

    public void disconnect() {
        if (onDisconnectCallback != null) {
            onDisconnectCallback.callback();
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

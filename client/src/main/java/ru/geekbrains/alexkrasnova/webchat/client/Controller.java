package ru.geekbrains.alexkrasnova.webchat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final String COMMAND_MESSAGE_SYMBOL = "/";
    private final String LOGIN = "/login ";
    private final String LOGIN_FAILED = "/login_failed ";
    private final String LOGIN_OK = "/login_ok ";
    private final String EXIT = "/exit";
    private final String CLEAR = "/clear";
    private final String CHANGE_ACCOUNT = "/change_account ";
    private final String CHANGE_NICKNAME = "/change_nickname ";
    private final String ERROR = "/error ";
    private final String CLIENTS_LIST = "/clients_list ";


    @FXML
    TextField loginField, messageField, passwordField;

    @FXML
    TextArea textArea;

    @FXML
    HBox loginPanel, messagePanel;

    @FXML
    ListView<String> clientsList;

    @FXML
    Button exitButton;

    @FXML
    VBox rightPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread listeningThread;
    private String username;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
    }

    public void setUsername(String username) {
        this.username = username;
        if (username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            messagePanel.setVisible(true);
            messagePanel.setManaged(true);
            rightPanel.setVisible(true);
            rightPanel.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            messagePanel.setVisible(false);
            messagePanel.setManaged(false);
            rightPanel.setVisible(false);
            rightPanel.setManaged(false);
        }
    }

    public void sendMessage() {
        try {
            String message = messageField.getText();
            if (message.equals(CLEAR)) {
                textArea.clear();
                return;
            } /*else if (message.startsWith(CHANGE_ACCOUNT)) {
                String[] tokens = message.split("\\s");
                if (tokens.length < 2) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым", ButtonType.OK);
                    alert.showAndWait();
                }
                exit();
                loginWithLoginAndPassword(tokens[1]);
                return;
            }*/
            out.writeUTF(message);
            messageField.clear();
            messageField.requestFocus();
            if (message.equals(EXIT)) {
                try {
                    listeningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                disconnect();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Процесс обмена сообщениями завершен", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }


    public void login() {

        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Логин и пароль не могут быть пустыми", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        loginWithLoginAndPassword(loginField.getText(), passwordField.getText());
    }

    public void loginWithLoginAndPassword(String login, String password) {

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(LOGIN + login + " " + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {

        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            listeningThread = new Thread(() -> {

                try {
                    // Цикл авторизации
                    while (true) {
                        String message = in.readUTF();
                        if (message.startsWith(LOGIN_OK)) {
                            setUsername(message.split("\\s")[1]);
                            break;
                        }
                        if (message.startsWith(LOGIN_FAILED)) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, message.split("\\s", 2)[1], ButtonType.OK);
                                alert.showAndWait();
                            });
                        }
                    }

                    // Цикл общения
                    while (true) {
                        String message = in.readUTF();
                        if (message.startsWith("/")) {
                            if (message.equals(EXIT)) {
                                textArea.clear();
                                break;
                            } else {
                                handleCommandMessage(message);
                                continue;
                            }
                        }
                        textArea.appendText(message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            listeningThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server [ localhost:8189 ]");
        }
    }

    public void disconnect() {
        setUsername(null);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        if (socket != null) {
            try {
                out.writeUTF(EXIT);
                messageField.clear();
                messageField.requestFocus();
                try {
                    listeningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                disconnect();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Процесс обмена сообщениями завершен", ButtonType.OK);
                alert.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }
    }

    private void handleCommandMessage(String message) {
        if (message.startsWith(ERROR)) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, message.split("\\s", 2)[1], ButtonType.OK);
                alert.showAndWait();
            });
            return;
        } else if (message.startsWith(CLIENTS_LIST)) {
            // /clients_list Bob Max Jack
            String[] tokens = message.split("\\s");

            Platform.runLater(() -> {
                System.out.println(Thread.currentThread().getName());
                clientsList.getItems().clear();
                for (int i = 1; i < tokens.length; i++) {
                    clientsList.getItems().add(tokens[i]);
                }
            });
            return;
        } else if (message.startsWith(CHANGE_NICKNAME)) {
            setUsername(message.split("\\s")[1]);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ваш новый ник: " + message.split("\\s", 2)[1], ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }

    }
}

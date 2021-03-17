package ru.geekbrains.alexkrasnova.webchat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller {

    private final String LOGIN = "/login ";
    private final String LOGIN_FAILED = "/login_failed ";
    private final String LOGIN_OK = "/login_ok ";
    private final String EXIT = "/exit";
    private final String CLEAR = "/clear";



    @FXML
    TextField usernameField, msgField;

    @FXML
    TextArea textArea;

    @FXML
    HBox loginPanel, msgPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread listeningThread;
    private String username;

    public void setUsername(String username) {
        this.username = username;
        if (username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
        }
    }

    public void sendMsg() {
        try {
            String msg = msgField.getText();
            out.writeUTF(msg);
            msgField.clear();
            msgField.requestFocus();
            if (msg.equals(EXIT)) {
                try {
                    listeningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socket.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Процесс обмена сообщениями завершен", ButtonType.OK);
                alert.showAndWait();
            } else if (msg.equals(CLEAR)) {
                textArea.clear();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }


    public void login() {

        if (socket == null || socket.isClosed()) {
            connect();
        }

        if (usernameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            out.writeUTF(LOGIN + usernameField.getText());
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
                        String msg = in.readUTF();
                        if (msg.startsWith(LOGIN_OK)) {
                            setUsername(msg.split("\\s")[1]);
                            break;
                        }
                        if (msg.startsWith(LOGIN_FAILED)) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, msg.split("\\s", 2)[1], ButtonType.OK);
                                alert.showAndWait();
                            });
                        }
                    }

                    // Цикл общения
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.equals("/exit")) {
                            break;
                        } else if (msg.startsWith("/error")) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, msg.split("\\s", 2)[1], ButtonType.OK);
                                alert.showAndWait();
                            });
                            continue;
                        }
                        textArea.appendText(msg + "\n");
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
}

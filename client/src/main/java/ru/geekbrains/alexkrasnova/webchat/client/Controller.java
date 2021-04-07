package ru.geekbrains.alexkrasnova.webchat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;

import org.apache.commons.io.FileUtils;

import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        boolean usernameIsNull = username == null;
        loginPanel.setVisible(usernameIsNull);
        loginPanel.setManaged(usernameIsNull);
        messagePanel.setVisible(!usernameIsNull);
        messagePanel.setManaged(!usernameIsNull);
        rightPanel.setVisible(!usernameIsNull);
        rightPanel.setManaged(!usernameIsNull);
    }

    public void sendMessage() {
        try {
            String message = messageField.getText();
            if (message.equals(CLEAR)) {
                textArea.clear();
                return;
            }
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

                    //todo: Изучить вопрос про BufferedReader, возможно лучше использовать его
                    File historyFile = new File(username + "-history.txt");
                    if (historyFile.exists()) {
                        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(username + "-history.txt"))) {
                            int x;
                            StringBuffer stringBuffer = new StringBuffer("");
                            while ((x = reader.read()) != -1) {
                                stringBuffer.append((char) x);
                            }
                            textArea.appendText(stringBuffer.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    String fileName = username + "-history.txt";
                    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName, true), StandardCharsets.UTF_8)) {

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
                            writer.write(message + "\n");
                            textArea.appendText(message + "\n");
                        }

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
            //todo: Попробовать изменять имя файла или пересохранять историю в новый файл при смене ника. Или хранить на клиенте логин, и файл истории привязывать к логину
            //String oldUsername = username;
            setUsername(message.split("\\s")[1]);
            /*File oldHistoryFile = new File(oldUsername + "-history.txt");
            File newHistoryFile = new File(username+"-history.txt");
            try {
                FileUtils.copyFile(oldHistoryFile, newHistoryFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            oldHistoryFile.delete();*/
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ваш новый ник: " + message.split("\\s", 2)[1], ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }

    }
}

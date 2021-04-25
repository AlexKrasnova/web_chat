package ru.geekbrains.alexkrasnova.webchat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final String COMMAND_MESSAGE_SYMBOL = "/";
    private final String LOGIN = "/login ";
    private final String LOGIN_FAILED = "/login_failed ";
    private final String LOGIN_OK = "/login_ok ";
    private final String CHANGE_ACCOUNT = "/change_account ";
    private final String EXIT = "/exit";
    private final String CLEAR = "/clear";
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

    private String username;
    private HistoryManager historyManager;
    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
        historyManager = new HistoryManager();
        network = new Network();

        network.setOnAuthOkCallback(args -> {
            String message = (String) args[0];
            setUsername(message.split("\\s")[2]);
            historyManager.init(message.split("\\s")[1]);
            textArea.clear();
            textArea.appendText(historyManager.load());
        });

        network.setOnAuthFailedCallback(args -> {
            String message = (String) args[0];
            Platform.runLater(() -> {
                showErrorAlert(message.split("\\s", 2)[1]);
            });
        });

        network.setOnMessageReceivedCallback(args -> {
            String message = (String) args[0];
            if (message.startsWith(COMMAND_MESSAGE_SYMBOL)) {
                handleCommandMessage(message);
                return;
            }
            historyManager.write(message + "\n");
            textArea.appendText(message + "\n");
        });

        network.setOnDisconnectCallback(args -> {
            setUsername(null);
            historyManager.close();
            textArea.clear();
        });
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
            network.sendMessage(messageField.getText());
            messageField.clear();
            messageField.requestFocus();
        } catch (IOException e) {
            showErrorAlert("Невозможно отправить сообщение");
        }
    }


    public void login() {

        if (loginField.getText().isEmpty() && passwordField.getText().isEmpty()) {
            showErrorAlert("Логин и пароль не могут быть пустыми");
            return;
        }

        if (!network.isConnected()) {
            try {
                network.connect(8189);
            } catch (IOException e) {
                showErrorAlert("Невозможно подключиться к серверу на порт: " + 8189);
                return;
            }
        }

        try {
            network.tryToLogin(loginField.getText(), passwordField.getText());
        } catch (IOException e) {
            showErrorAlert("Невозможно отправить данные пользователя");
        }
    }

    public void exit() {
        try {
            network.sendMessage(EXIT);
        } catch (IOException e) {
            showErrorAlert("Невозможно выйти");
        }
    }

    private void handleCommandMessage(String message) {
        if (message.startsWith(ERROR)) {
            Platform.runLater(() -> {
                showErrorAlert(message.split("\\s", 2)[1]);
            });
            return;
        } else if (message.startsWith(CLIENTS_LIST)) {
            String[] tokens = message.split("\\s");

            Platform.runLater(() -> {
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

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.setTitle("Web Chat FX");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

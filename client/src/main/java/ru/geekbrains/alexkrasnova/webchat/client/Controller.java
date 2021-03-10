package ru.geekbrains.alexkrasnova.webchat.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextField msgField;

    TextField textField;

    @FXML
    TextArea textArea;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Thread listeningThread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            listeningThread = new Thread(() -> {

                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.equals("/exit")) {
                            break;
                        }
                        textArea.appendText(msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            listeningThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server [ localhost:8189 ]");
        }

    }

    public void sendMsg() {
        try {
            String msg = msgField.getText();
            out.writeUTF(msg);
            msgField.clear();
            if (msg.equals("/exit")) {
                try {
                    listeningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socket.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Процесс обмена сообщениями завершен", ButtonType.OK);
                alert.showAndWait();
            }

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }


}

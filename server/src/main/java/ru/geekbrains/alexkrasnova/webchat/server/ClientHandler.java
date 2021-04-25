package ru.geekbrains.alexkrasnova.webchat.server;

import org.apache.logging.log4j.Level;
import ru.geekbrains.alexkrasnova.webchat.server.exception.InvalidCommandMessageException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.UsernameAlreadyExistsException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.AuthenticationException;
import ru.geekbrains.alexkrasnova.webchat.server.exception.NoSuchClientException;
import ru.geekbrains.alexkrasnova.webchat.server.user.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private final String COMMAND_MESSAGE_SYMBOL = "/";
    private final String EXIT = "/exit";
    private final String LOGIN = "/login ";
    private final String LOGIN_OK = "/login_ok ";
    private final String LOGIN_FAILED = "/login_failed ";
    private final String WHO_AM_I = "/who_am_i";
    private final String PRIVATE_MESSAGE = "/w ";
    private final String ERROR = "/error ";
    private final String CHANGE_NICKNAME = "/change_nickname ";


    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private User user;

    public String getUsername() {
        return user.getUsername();
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.server.getExecutorService().execute(() -> {
            try {
                // Цикл авторизации
                login();

                // Цикл общения
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith(COMMAND_MESSAGE_SYMBOL) && !message.startsWith(LOGIN)) {
                        if (message.equals(EXIT)) {
                            out.writeUTF(message);
                            this.socket.close();
                            server.unsubscribe(this);
                            System.out.println("Клиент отключился");
                            break;
                        }
                        try {
                            handleCommandMessage(message);
                        } catch (InvalidCommandMessageException e) {
                            sendMessage(ERROR + e.getMessage());
                            Server.LOGGER.debug("Ошибка ввода пользовательских данных: " + e.getMessage(), e);
                        }
                        continue;
                    }
                    server.broadcastMessage(user.getUsername() + ": " + message);
                }
            } catch (IOException e) {
                Server.LOGGER.throwing(Level.ERROR, e);
            } finally {
                disconnect();
            }
        });
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Server.LOGGER.throwing(Level.ERROR, e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            Server.LOGGER.throwing(Level.ERROR, e);
            disconnect();
        }

    }

    private void handleCommandMessage(String message) throws IOException, InvalidCommandMessageException {
        if (message.equals(WHO_AM_I)) {
            sendMessage("Ваш ник: " + user.getUsername());
        } else if (message.startsWith(PRIVATE_MESSAGE)) {
            String[] privatMessageArray = message.split("\\s");
            if (privatMessageArray.length >= 3) {
                String addresseeUsername = message.split("\\s", 3)[1];
                String msg = message.split("\\s", 3)[2];
                try {
                    server.sendMessage(this, addresseeUsername, msg);
                } catch (NoSuchClientException e) {
                    sendMessage(ERROR + e.getMessage());
                    Server.LOGGER.debug("Ошибка ввода пользовательских данных: " + e.getMessage(), e);
                }
            } else if (privatMessageArray.length <= 1) {
                throw new InvalidCommandMessageException("Имя адресата не может быть пустым");
            } else {
                throw new InvalidCommandMessageException("Текст личного сообщения не может быть пустым");
            }
        } else if (message.startsWith(PRIVATE_MESSAGE.split("\\s")[0])) {
            throw new InvalidCommandMessageException("Имя адресата не может быть пустым");
        } else if (message.startsWith(CHANGE_NICKNAME)) {
            String[] tokens = message.split("\\s");
            if (tokens.length < 2) {
                throw new InvalidCommandMessageException("Имя пользователя не может быть пустым");
            }
            String oldUsername = user.getUsername();
            try {
                user = server.getUserService().changeUsernameAndGetUser(user.getLogin(), tokens[1]);
                sendMessage(message);
                server.broadcastMessage("Клиент " + oldUsername + " сменил ник на " + user.getUsername());
                server.broadcastClientsList();
            } catch (UsernameAlreadyExistsException e) {
                sendMessage("/error " + e.getMessage());
                Server.LOGGER.debug("Ошибка ввода пользовательских данных: " + e.getMessage(), e);
            }
            /*            if(SERVER.isUserOnline(tokens[1])){
                sendMessage("/error Данное имя пользователя уже занято");
                return;
            }

            username = tokens[1];*/

        } else if (message.startsWith(CHANGE_NICKNAME.split("\\s")[0])) {
            throw new InvalidCommandMessageException("Имя пользователя не может быть пустым");
        }

    }

    private void login() throws IOException {
        while (true) {
            String message = in.readUTF();
            try {
                tryToLogin(message);
                break;
            } catch (AuthenticationException e) {
                sendMessage(LOGIN_FAILED + " " + e.getMessage());
                Server.LOGGER.debug("Ошибка ввода пользовательских данных: " + e.getMessage(), e);
            }
        }
    }

    private void tryToLogin(String message) throws AuthenticationException {
        String[] tokens = message.split("\\s");
        String login = tokens[1];
        String password = tokens[2];
        User userTemp = server.getUserService().checkCredentialsAndGetUser(login, password);
        if (server.isUserOnline(userTemp)) {
            throw new AuthenticationException("Данная учетная запись уже используется");
        }
        user = userTemp;
        sendMessage(LOGIN_OK + user.getLogin() + " " + user.getUsername());
        server.subscribe(this);
    }

    public User getUser() {
        return user;
    }
}

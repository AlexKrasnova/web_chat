package ru.geekbrains.alexkrasnova.webchat.client;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HistoryManager {
    private String login;
    private OutputStream out;

    public void init(String login) {
        try {
            this.login = login;
            out = new FileOutputStream(getFileName(), true);
        } catch (IOException e) {
            throw new RuntimeException("Проблема при работе с историей");
        }
    }

    public void write(String message) {
        try {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Проблема при работе с историей");
        }
    }

    public String load() {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(getFileName()))) {
            String str;
            while ((str = in.readLine()) != null) {
                stringBuilder.append(str).append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException("Проблема при работе с историей");
        }
    }

    public void close() {
        login = null;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileName() {
        return "history/history_" + login + ".txt";
    }
}

package ru.gb.javafxmessenger.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {

    private final List<ClientHandler> clients;
    private final AuthService authService;
    private final Map<String, Path> clientsPaths = new HashMap<>();

    public ChatServer() {
        this.clients = new ArrayList<>();
        this.authService = new SQLAuthService();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new SQLAuthService()) {
            createMessageHistoryFile();
            while (true) {
                System.out.println("Ожидаю подключения...");
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключен.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastAndWriteHistory(String message) {
        clients.forEach(client -> client.sendMessage(message));
        clientsPaths.forEach((nick, path) -> {
            try {
                Files.writeString(path, message + "\n", StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void privateMessage(String recipient, String message, String sender) {
        clients.forEach(client -> {
            if (client.getNick().equals(recipient)) {
                client.sendMessage("Личное сообщение от " + sender + ": " + message);
            }
            if (client.getNick().equals(sender)) {
                client.sendMessage("Вы отправили сообщение пользователю " + recipient + ": " + message);
            }
        });
        clientsPaths.forEach((nick, path) -> {
            if (nick.equals(recipient)) {
                try {
                    Files.writeString(path, "Личное сообщение от " + sender + ": " + message + "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (nick.equals(sender)) {
                try {
                    Files.writeString(path, "Вы отправили сообщение пользователю " + recipient + ": " + message + "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void createMessageHistoryFile() {
        createPathsForClients();
        clientsPaths.forEach((nick, path) -> {
            Path parent = path.getParent();
            if (!Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createPathsForClients() {
        try {
            ResultSet rs = SQLAuthService.readTable();
            while (rs.next()) {
                String nickName = rs.getString(2);
                String login = rs.getString(1);
                clientsPaths.put(rs.getString(2), Path.of(
                        "D:\\Java prog\\java-fx-messenger\\src\\main\\resources\\history\\history_" +
                                rs.getString(1) + ".txt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> restorationMessages(String nick) {
        Path path = clientsPaths.get(nick);
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

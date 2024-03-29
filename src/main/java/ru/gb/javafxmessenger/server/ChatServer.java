package ru.gb.javafxmessenger.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> clients;
    private final AuthService authService;

    public ChatServer() {
        this.clients = new ArrayList<>();
        this.authService = new SQLAuthService();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new SQLAuthService()) {
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

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
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
        for (ClientHandler client : clients) {
            if (client.getNick().equals(recipient)) {
                client.sendMessage("Личное сообщение от " + sender + ": " + message);
            }
            if (client.getNick().equals(sender)) {
                client.sendMessage("Вы отправили сообщение пользователю " + recipient + ": " + message);
            }
        }
    }
}

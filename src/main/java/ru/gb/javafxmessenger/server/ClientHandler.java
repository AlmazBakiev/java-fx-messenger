package ru.gb.javafxmessenger.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authenticate();
                    readMessage();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        while (true) {
            try {
                String message = in.readUTF();
                if (message.startsWith("/auth")) {
                    String[] split = message.split("\\p{Blank}+");
                    String login = split[1];
                    String password = split[2];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            System.out.println("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользовалель " + nick + " зашел в чат");
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage("неверный логин и пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end");
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        while (true) {
            try {
                String message = in.readUTF();
                if ("/end".equalsIgnoreCase(message)) {
                    break;
                }
                String[] split = message.split("\\p{Blank}+", 3);
                if ("/w".equalsIgnoreCase(split[0])) {
                    server.privateMessage(split[1], split[2], nick);
                    continue;
                }
                if ("/change".equalsIgnoreCase(split[0])) {
                    server.broadcast("Пользовалетль, " + nick + ", сменил ник на " + split[1]);
                    nick = ((SQLAuthService) authService).changeNickName(nick, split[1]);
                    continue;
                }
                server.broadcast(nick + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNick() {
        return nick;
    }
}

package ru.gb.javafxmessenger.server;

import java.io.IOException;
import java.sql.*;

public class SQLAuthService implements AuthService {

    private static Connection connection;
    private static Statement statement;

    public SQLAuthService() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:D:\\Java prog\\java-fx-messenger\\src\\main\\resources\\auth.db");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement("select nick_name from auth where login = ? and password = ?");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            String nick = rs.getString("nick_name");
            return nick;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    String changeNickName(String firstNickName, String secondNickName) {
        ResultSet rs;
        try {
            rs = statement.executeQuery(
                    "select login from auth where nick_name = '" + firstNickName + "'");
            statement.executeUpdate(
                    "update auth set nick_name = '" + secondNickName + "' where login = '" + rs.getString(1) + "'");
            rs = statement.executeQuery(
                    "select nick_name from auth where login = '" + rs.getString(1) + "'");
            return rs.getString(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

package ru.hse.cs.java2020.task03;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class DataBase {

    private Connection connection;
    private final String url;

    DataBase(String path) {
        url = ("jdbc:postgresql:" + path);
    }

    void connect() {
        try {
            connection = DriverManager.getConnection(url, "postgres", "root");
            createTable();
            System.err.println("Сonnected");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS DB"
                + "( chatId integer NOT NULL,"
                + " oAuth text NOT NULL,"
                + " orgId text NOT NULL,"
                + " login text NOT NULL,"
                + "PRIMARY KEY (chatId)"
                + ");";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    Optional<UserData> get(long chatId) {
        String sql = "SELECT oAuth, orgId, login FROM public.DB WHERE chatId = '" + chatId + "'";
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            result.next();
            return Optional.of(new UserData(result.getString("oAuth"), result.getString("orgId"),
                    result.getString("login")));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }


    void insert(long chatId, UserData info) {
        String sql = "INSERT INTO DB VALUES('"
                + chatId + "','" + info.getToken() + "','" + info.getOrg() + "','" + info.getLogin()
                + "') ON CONFLICT (chatId) DO UPDATE SET oAuth = '" + info.getToken() + "', orgId = '" + info.getOrg()
                 + "', login = '" + info.getLogin() + "';";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

}

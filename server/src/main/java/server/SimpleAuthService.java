package server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();

        try {
            Server.statement = Server.connection.createStatement();
            ResultSet userList = Server.statement.executeQuery("SELECT * FROM users");
            while (userList.next()){
                users.add(new UserData(userList.getString("login"),
                        userList.getString("password"),userList.getString("nickname")));
            }
            userList.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }




    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if(user.login.equals(login) && user.password.equals(password)){
                return user.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData user : users) {
            if(user.login.equals(login) || user.nickname.equals(nickname)){
                return false;
            }
        }
        try {
            Server.statement.executeUpdate(String.format("INSERT INTO users (login, password, nickname) " +
                    "VALUES (%s, %s, %s)", login, password, nickname));
            users.add(new UserData(login, password, nickname));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return true;
    }
}

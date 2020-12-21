package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;



    public Server() {

        try {
            connectDB();
            System.out.println("DB connected!");

        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();



        try {
            server = new ServerSocket(PORT);
            System.out.println("server started!");



            while (true) {
                socket = server.accept();
                System.out.println("client connected " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("server closed");
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            disconnectDB();
        }



    }


    public static Connection connection;
    public static Statement statement;

    public static void connectDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/src/main/resources/chatfx.db");
    }

    public static void disconnectDB(){
        try {
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("%s : %s", sender.getNickname(), msg);

        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] private [ %s ] : %s", sender.getNickname(), receiver, msg);

        for (ClientHandler c : clients) {
            if (c.getNickname().equals(receiver)) {
                c.sendMsg(message);
                if (!c.equals(sender)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg("Not found user: " + receiver);
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isloginAuthenticated(String login){
        for (ClientHandler c : clients) {
            if(c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler c : clients) {
            sb.append(c.getNickname()).append(" ");
        }

        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}

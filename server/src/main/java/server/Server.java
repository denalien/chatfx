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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;



    public Server() {

        ExecutorService service = Executors.newCachedThreadPool();

        try {
            connectDB();
//            System.out.println("DB connected!");
            ServerLogs.logger.log(Level.CONFIG,"DB connected!");


        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();



        try {
            server = new ServerSocket(PORT);
//            System.out.println("server started!");
            ServerLogs.logger.info("Server started!");


            while (true) {
                socket = server.accept();
//                System.out.println("client connected " + socket.getRemoteSocketAddress());
                ServerLogs.logger.info("Client connected " + socket.getRemoteSocketAddress());

                /*
                Использование здесь CashedThreadPool позволяет немного выиграть в ресурсах
                при условии, что данным сетевым чатом пользуется большое количество людей.
                В таком случае нам не придется создавать новый поток при каждом новом подключении,
                если в пуле есть свободные.
                Также это позволит слегка выиграть в случае, если клиент просто перезашел после
                обрыва связи.

                Использование SingleThreadExecutor в данном случае полностью лишено смысла, поскольку
                дает подключение только одному клиенту за раз.

                Можно использовать FixedThreadPool в том случае, если чат приватный и мы заранее знаем,
                сколько пользователей будут им пользоваться (имеет смысл, например, чтобы не пускать третьего
                человека в чат). В остальных случаях лучше пользоваться или изначальной реализацией,
                или CashedThreadPool.
                 */
                service.execute(new ClientHandler(this, socket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("server closed");
            ServerLogs.logger.info("Server closed!");

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
            service.shutdown();
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

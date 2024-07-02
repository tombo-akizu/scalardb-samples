package sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.io.*;
import java.net.*;
import com.scalar.db.service.TransactionFactory;
import sample.command.GameRequestHandler;
import sample.command.LoginRequestHandler;
import sample.command.MultiGameRequestHandler;
import sample.command.MultiGameRequestHandler.MatchResult;

public class TCPIPServer {
    private final TransactionFactory factory;
    private List<User> usersInRoom;
    private final int ROOM_SIZE = 2;

    public TCPIPServer(TransactionFactory factory) {
        this.factory = factory;
        this.usersInRoom = new ArrayList<User>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345");
            while (true) {
                Socket socket = serverSocket.accept();
                new ServerThread(socket, factory, this).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void register(User user) {
        usersInRoom.add(user);
        user.send_message("M");
        user.send_message("Matching...");
        if (usersInRoom.size() == ROOM_SIZE) {
            usersInRoom.forEach(u -> u.send_message("Game matched!"));
            usersInRoom.forEach(u -> u.send_message("H"));
            System.out.println("Match!");
        }
    }

    public synchronized void process_hand(TransactionFactory factory, int id, int hand, PrintWriter writer) {
        boolean all_hand_decided = true;
        Iterator<User> iter = usersInRoom.iterator();
        while (iter.hasNext()) {
            User u = iter.next();
            if (u.get_id() == id) {
                u.set_hand(hand);
            } else {
                if (u.get_hand() == -1) {
                    all_hand_decided = false;
                }
            }
        }
        if (all_hand_decided) {
            // With this implement, we can't process multiple matches...
            if (usersInRoom.get(0).get_hand() == usersInRoom.get(1).get_hand()) {
                usersInRoom.forEach(u -> u.send_message("D"));
                usersInRoom.forEach(u -> new MultiGameRequestHandler(factory, u.get_id(), MatchResult.DRAW, writer).run());
            } else if (
                (usersInRoom.get(0).get_hand() == 0 && usersInRoom.get(1).get_hand() == 2)
                || (usersInRoom.get(0).get_hand() == 2 && usersInRoom.get(1).get_hand() == 5)
                || (usersInRoom.get(0).get_hand() == 5 && usersInRoom.get(1).get_hand() == 0)
            ) {
                usersInRoom.get(0).send_message("W");
                usersInRoom.get(1).send_message("L");
                new MultiGameRequestHandler(factory, usersInRoom.get(0).get_id(), MatchResult.WIN, writer).run();
                new MultiGameRequestHandler(factory, usersInRoom.get(1).get_id(), MatchResult.LOSE, writer).run();
            } else {
                usersInRoom.get(0).send_message("L");
                usersInRoom.get(1).send_message("W");
                new MultiGameRequestHandler(factory, usersInRoom.get(0).get_id(), MatchResult.LOSE, writer).run();
                new MultiGameRequestHandler(factory, usersInRoom.get(1).get_id(), MatchResult.WIN, writer).run();
            }
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;
    private TransactionFactory factory;
    private TCPIPServer server;

    public ServerThread(Socket socket, TransactionFactory factory, TCPIPServer server) {
        this.socket = socket;
        this.factory = factory;
        this.server = server;
    }

    public void run() {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            String request;
            while ((request = reader.readLine()) != null) {
                System.out.println("Received: " + request);
                String[] parts = request.split(" ");
                String command = parts[0];
                int userId = Integer.parseInt(parts[1]);

                if ("LOGIN".equalsIgnoreCase(command)) {
                    new LoginRequestHandler(factory, userId, writer).run();
                } else if ("GAME".equalsIgnoreCase(command)) {
                    int coin = Integer.parseInt(parts[2]);
                    int choice = Integer.parseInt(parts[3]);
                    new GameRequestHandler(factory, userId, coin, choice, writer).run();
                } else if ("MULTIGAME_MATCH".equalsIgnoreCase(command)) {
                    System.out.println("Multi Game Accepted");
                    server.register(new User(userId, writer));
                } else if ("MULTIGAME_HAND".equalsIgnoreCase(command)) {
                    System.out.println("Hand Accepted");
                    int hand = Integer.parseInt(parts[2]);
                    server.process_hand(factory, userId, hand, writer);
                } else {
                    writer.println("Invalid command");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

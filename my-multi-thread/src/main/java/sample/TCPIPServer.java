package sample;

import java.io.*;
import java.net.*;
import com.scalar.db.service.TransactionFactory;
import sample.command.GameRequestHandler;
import sample.command.LoginRequestHandler;

public class TCPIPServer {
    private final TransactionFactory factory;

    public TCPIPServer(TransactionFactory factory) {
        this.factory = factory;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345");
            while (true) {
                Socket socket = serverSocket.accept();
                new ServerThread(socket, factory).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;
    private TransactionFactory factory;

    public ServerThread(Socket socket, TransactionFactory factory) {
        this.socket = socket;
        this.factory = factory;
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
                } else {
                    writer.println("Invalid command");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

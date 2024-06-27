package sample;

import java.io.*;
import java.net.*;

public class TCPIPClient {
    private enum State {
        NEUTRAL,
        MULTIGAME_MATCHING,
        MULTIGAME_WAITINGHAND,
        MULTIGAME_WAITINGRESULT;
    }
    private static State currentState = State.NEUTRAL;

    public static void main(String[] args) {
        String hostname = "127.0.0.1";
        int port = 12345;

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to the server");

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String text = null;

            InputStream input = socket.getInputStream();
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(input));
            String response;

            do {
                switch (currentState) {
                    case NEUTRAL:
                        System.out.print("Enter text: ");
                        text = reader.readLine();
                        System.out.println("Read from input: " + text);  // デバッグメッセージ
                        if (text != null && !text.equals("bye")) {
                            writer.println(text);
                            System.out.println("Sent to server: " + text);  // デバッグメッセージ
                            response = serverReader.readLine();
                            System.out.println("Received from server: " + response);

                            if (response.equals("M")) {
                                currentState = State.MULTIGAME_MATCHING;
                            }
                        }
                        break;
                    case MULTIGAME_MATCHING:
                        response = serverReader.readLine();
                        System.out.println(response);
                        if (response.equals("H")) {
                            currentState = State.MULTIGAME_WAITINGHAND;
                        }
                        break;
                    case MULTIGAME_WAITINGHAND:
                        System.out.println("input hand: 0, 2 or 5");
                        System.out.println("ex. MULTIGAME_HAND userid hand");
                        text = reader.readLine();
                        writer.println(text);
                        currentState = State.MULTIGAME_WAITINGRESULT;
                        break;
                    case MULTIGAME_WAITINGRESULT:
                        System.out.println("Waiting Opponent's Hand");
                        response = serverReader.readLine();
                        System.out.println(response);
                        currentState = State.NEUTRAL;
                        break;
                }
            } while (text != null && !text.equals("bye"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

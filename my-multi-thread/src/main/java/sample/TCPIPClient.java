package sample;

import java.io.*;
import java.net.*;

public class TCPIPClient {
    public static void main(String[] args) {
        String hostname = "127.0.0.1";
        int port = 12345;

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to the server");

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String text;

            do {
                System.out.print("Enter text: ");
                text = reader.readLine();
                System.out.println("Read from input: " + text);  // デバッグメッセージ
                if (text != null && !text.equals("bye")) {
                    writer.println(text);
                    System.out.println("Sent to server: " + text);  // デバッグメッセージ

                    InputStream input = socket.getInputStream();
                    BufferedReader serverReader = new BufferedReader(new InputStreamReader(input));
                    String response = serverReader.readLine();
                    System.out.println("Received from server: " + response);
                }
            } while (text != null && !text.equals("bye"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

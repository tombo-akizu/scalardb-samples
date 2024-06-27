package sample;
import com.scalar.db.service.TransactionFactory;
import java.io.*;
import java.net.*;

public class User {
    private final int id;
    private final PrintWriter writer;
    private int hand;

    public User(int id, PrintWriter writer) {
        this.id = id;
        this.writer = writer;
        hand = -1;
    }

    public int get_id() {
        return id;
    }

    public void send_message(String message) {
        this.writer.println(message);
    }

    public int get_hand() {
        return hand;
    }

    public void set_hand(int hand) {
        this.hand = hand;
    }
}
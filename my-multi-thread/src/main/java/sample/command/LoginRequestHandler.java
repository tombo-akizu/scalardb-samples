package sample.command;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Result;
import com.scalar.db.io.IntValue;
import com.scalar.db.service.TransactionFactory;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;

import java.io.PrintWriter;
import java.util.Optional;

public class LoginRequestHandler implements Runnable {
    private final DistributedTransactionManager manager;
    private final int userId;
    private final PrintWriter writer;

    public LoginRequestHandler(TransactionFactory factory, int userId, PrintWriter writer) {
        this.manager = factory.getTransactionManager();
        this.userId = userId;
        this.writer = writer;
    }

    @Override
    public void run() {
        DistributedTransaction tx = null;
        try {
            tx = manager.start();
            Get get = new Get(new Key(new IntValue("user_id", userId)))
                    .forNamespace("your_namespace")
                    .forTable("user");
            Optional<Result> result = tx.get(get);

            if (result.isPresent()) {
                int coin = result.get().getValue("coin").get().getAsInt();
                writer.println("LOGIN SUCCESS " + coin);
            } else {
                writer.println("LOGIN FAIL");
            }

            tx.commit();
        } catch (TransactionException e) {
            if (tx != null) {
                try {
                    tx.abort();
                } catch (TransactionException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            writer.println("Error fetching user data");
        }
    }
}

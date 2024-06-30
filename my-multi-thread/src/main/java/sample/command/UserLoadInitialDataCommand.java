package sample.command;

import com.scalar.db.api.Put;
import com.scalar.db.io.IntValue;
import com.scalar.db.api.DistributedStorage;
import com.scalar.db.service.TransactionFactory;

import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.io.Key;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;

public class UserLoadInitialDataCommand {
    private final DistributedTransactionManager manager;
    private static final String NAMESPACE = "your_namespace";
    private static final String TABLE_NAME = "user";

    public UserLoadInitialDataCommand(TransactionFactory factory) {
        this.manager = factory.getTransactionManager();
    }

    public void start() {
        DistributedTransaction tx = null;
        try {
            // Insert initial data
            tx = manager.start();
            Put put1 = new Put(new Key(new IntValue("user_id", 1)))
                    .withValue(new IntValue("coin", 100))
                    .forNamespace(NAMESPACE)
                    .forTable(TABLE_NAME);
            tx.put(put1);

            Put put2 = new Put(new Key(new IntValue("user_id", 2)))
                    .withValue(new IntValue("coin", 200))
                    .forNamespace(NAMESPACE)
                    .forTable(TABLE_NAME);
            tx.put(put2);

            tx.commit();
        } catch (TransactionException e) {
            e.printStackTrace();
        }
    }
}

package sample.command;

import com.scalar.db.api.Put;
import com.scalar.db.io.IntValue;
import com.scalar.db.api.DistributedStorage;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.io.Key;

public class UserLoadInitialDataCommand implements Runnable {
    private final DistributedStorage storage;
    private static final String NAMESPACE = "your_namespace";
    private static final String TABLE_NAME = "user";

    public UserLoadInitialDataCommand(DistributedStorage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        try {
            // Insert initial data
            Put put1 = new Put(new Key(new IntValue("user_id", 1)))
                    .withValue(new IntValue("coin", 100))
                    .forNamespace(NAMESPACE)
                    .forTable(TABLE_NAME);
            storage.put(put1);

            Put put2 = new Put(new Key(new IntValue("user_id", 2)))
                    .withValue(new IntValue("coin", 200))
                    .forNamespace(NAMESPACE)
                    .forTable(TABLE_NAME);
            storage.put(put2);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

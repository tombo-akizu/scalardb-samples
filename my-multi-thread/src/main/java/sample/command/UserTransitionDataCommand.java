package sample.command;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Delete;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.service.TransactionFactory;
import com.scalar.db.io.Key;
import com.scalar.db.io.IntValue;
import com.scalar.db.exception.storage.ExecutionException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UserTransitionDataCommand {

    private final DistributedTransactionManager manager;
    private final String SOURCE_NAMESPACE =  System.getenv("SOURCE_NAMESPACE");
    private final String DESTINATION_NAMESPACE = System.getenv("DESTINATION_NAMESPACE");  // Cassandraの名前空間
    private final String TABLE_NAME = "user";

    public UserTransitionDataCommand(TransactionFactory factory) {
        this.manager = factory.getTransactionManager();
    }

    public void start() {
        try {
            // source_namespaceからデータを取得するためにトランザクションを開始
            DistributedTransaction readTx = manager.start();

            // スキャンのためのパーティションキーを指定
            // すべてのユーザーをスキャンするために適切なパーティションキーの範囲を使用
            for (int partitionKey = 0; partitionKey <= 100; partitionKey++) { 
                Scan scan = new Scan(new Key(new IntValue("user_id", partitionKey)))
                        .forNamespace(SOURCE_NAMESPACE)
                        .forTable(TABLE_NAME);

                List<Result> results = readTx.scan(scan);

                // 取得した全てのデータをdestination_namespaceに追加
                for (Result result : results) {
                    int userId = ((IntValue) result.getValue("user_id").get()).get();
                    int coin = ((IntValue) result.getValue("coin").get()).get();

                    // destination_namespaceにデータを書き込むためにトランザクション開始
                    DistributedTransaction writeTx = manager.start();

                    // destination_namespaceにあるデータに同じユーザデータがある場合は削除
                    Get get = new Get(new Key(new IntValue("user_id", userId)))
                            .forNamespace(DESTINATION_NAMESPACE)
                            .forTable(TABLE_NAME);
                    Optional<Result> existingResult = writeTx.get(get);
                    if (existingResult.isPresent()) {
                        Delete delete = new Delete(new Key(new IntValue("user_id", userId)))
                            .forNamespace(DESTINATION_NAMESPACE)
                            .forTable(TABLE_NAME);
                        writeTx.delete(delete);
                    }

                    // 新しいレコードを挿入
                    Put put = new Put(new Key(new IntValue("user_id", userId)))
                            .withValue(new IntValue("coin", coin))
                            .forNamespace(DESTINATION_NAMESPACE)
                            .forTable(TABLE_NAME);
                    writeTx.put(put);
                    writeTx.commit();
                }
            }

            readTx.commit();
        } catch (TransactionException e) {
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }
}

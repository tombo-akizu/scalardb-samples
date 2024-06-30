package sample.command;

import com.scalar.db.api.Put;
import com.scalar.db.api.Get;
import com.scalar.db.api.Result;
import com.scalar.db.io.IntValue;
import com.scalar.db.service.TransactionFactory;

import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.Random;

import javax.print.attribute.standard.MediaSize.NA;

public class MultiGameRequestHandler implements Runnable {
    private final DistributedTransactionManager manager;
    private final int userId;
    private final MatchResult matchResult;
    private final PrintWriter writer;
    private static final String NAMESPACE = System.getenv("NAMESPACE");
    private static final String TABLE_NAME = "user";

    public enum MatchResult {
        WIN,
        LOSE,
        DRAW;
    }

    public MultiGameRequestHandler(TransactionFactory factory, int userId, MatchResult matchResult, PrintWriter writer) {
        this.manager = factory.getTransactionManager();
        this.userId = userId;
        this.matchResult = matchResult;
        this.writer = writer;
    }

    @Override
    public void run() {
        DistributedTransaction tx = null;
        try {
            tx = manager.start();
            // ユーザ情報を取得
            Get get = new Get(new Key(new IntValue("user_id", userId)))
                    .forNamespace(NAMESPACE)
                    .forTable(TABLE_NAME);
            Optional<Result> result = tx.get(get);

            if (result.isPresent()) {
                int currentCoin = result.get().getValue("coin").get().getAsInt();

                // コインの更新
                int updatedCoin = currentCoin + switch (matchResult) {
                    case WIN -> 100;
                    case LOSE -> 10;
                    case DRAW -> 50;
                };

                // データベースに更新
                Put put = new Put(new Key(new IntValue("user_id", userId)))
                        .withValue(new IntValue("coin", updatedCoin))
                        .forNamespace(NAMESPACE)
                        .forTable(TABLE_NAME);
                tx.put(put);
            } else {
                writer.println("User not found");
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
            writer.println("Error updating user data");
        }
    }
}

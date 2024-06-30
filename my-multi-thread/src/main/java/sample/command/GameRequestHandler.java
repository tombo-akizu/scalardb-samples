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

public class GameRequestHandler implements Runnable {
    private final DistributedTransactionManager manager;
    private final int userId;
    private final int coin;
    private final int choice;
    private final PrintWriter writer;
    private static final String NAMESPACE = System.getenv("NAMESPACE");
    private static final String TABLE_NAME = "user";

    public GameRequestHandler(TransactionFactory factory, int userId, int coin, int choice, PrintWriter writer) {
        this.manager = factory.getTransactionManager();
        this.userId = userId;
        this.coin = coin;
        this.choice = choice;
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

                // ゲームの結果を判定
                Random rand = new Random();
                int dice1 = rand.nextInt(6) + 1;
                int dice2 = rand.nextInt(6) + 1;
                boolean isWin = ((dice1 + dice2) % 2 == choice);

                // コインの更新
                int updatedCoin = isWin ? currentCoin + coin : currentCoin - coin;

                // データベースに更新
                Put put = new Put(new Key(new IntValue("user_id", userId)))
                        .withValue(new IntValue("coin", updatedCoin))
                        .forNamespace(NAMESPACE)
                        .forTable(TABLE_NAME);
                tx.put(put);

                // 結果をクライアントに送信
                if (isWin) {
                    writer.println("GAME WIN " + dice1 + " " + dice2 + " " + updatedCoin);
                } else {
                    writer.println("GAME LOSE " + dice1 + " " + dice2 + " " + updatedCoin);
                }
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

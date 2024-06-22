package sample.command;

import com.scalar.db.api.Get;
import com.scalar.db.api.Result;
import com.scalar.db.io.IntValue;
import com.scalar.db.api.DistributedStorage;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.io.Key; // 修正

import java.util.Optional;

public class FetchUserCoinCommand implements Runnable {
    private final DistributedStorage storage;
    private final int userId;
    private static final String NAMESPACE = "your_namespace"; // 名前空間を設定
    private static final String TABLE_NAME = "user"; // テーブル名を設定

    public FetchUserCoinCommand(DistributedStorage storage, int userId) {
        this.storage = storage;
        this.userId = userId;
    }

    @Override
    public void run() {
        try {
            Get get = new Get(new Key(new IntValue("user_id", userId)))
                    .forNamespace(NAMESPACE) // 名前空間を設定
                    .forTable(TABLE_NAME); // テーブル名を設定
            Optional<Result> result = storage.get(get);

            result.ifPresent(r -> {
                int coin = r.getValue("coin").get().getAsInt();
                System.out.println("User ID: " + userId + ", Coin: " + coin);
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

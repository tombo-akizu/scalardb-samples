package sample;

import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.service.StorageFactory;
import com.scalar.db.service.TransactionFactory;
import sample.command.UserLoadInitialDataCommand;
import sample.command.UserTransitionDataCommand;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        // 共通の設定を読み込む
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("database.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        DatabaseConfig config = new DatabaseConfig(props);
        TransactionFactory transactionFactory = new TransactionFactory(config);
        StorageFactory storageFactory = new StorageFactory(config);

        // サーバーを別スレッドで起動
        if (args.length > 0 && args[0].equals("server")) {
            TCPIPServer server = new TCPIPServer(transactionFactory);
            new Thread(server::start).start();
        } else if (args.length > 0 && args[0].equals("client")) {
            TCPIPClient.main(args);
        } else if (args.length > 0 && args[0].equals("loadInitialData")) {
            // データベースに初期データをロード
            try {
                new UserLoadInitialDataCommand(transactionFactory).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length > 0 && args[0].equals("transitionData")) {
            // 2つのデータベース間でデータを移行
            try {
                new UserTransitionDataCommand(transactionFactory).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Usage: java -jar myapp.jar <server|client>");
        }
    }
}

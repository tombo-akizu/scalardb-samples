package sample;

import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.service.StorageFactory;
import com.scalar.db.service.TransactionFactory;
import sample.command.UserLoadInitialDataCommand;

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

            // 初期データのロード
            try {
                new UserLoadInitialDataCommand(storageFactory.getStorage()).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length > 0 && args[0].equals("client")) {
            TCPIPClient.main(args);
        } else {
            System.out.println("Usage: java -jar myapp.jar <server|client>");
        }
    }
}

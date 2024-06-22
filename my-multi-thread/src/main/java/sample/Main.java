package sample;

import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.service.StorageFactory;
import sample.command.UserLoadInitialDataCommand;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;
import com.scalar.db.service.TransactionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        // サーバーを別スレッドで起動
        if (args.length > 0 && args[0].equals("server")) {
            TCPIPServer.main(args);
            // 初期データのロード
            try (FileInputStream fis = new FileInputStream("database.properties")) {
                Properties props = new Properties();
                props.load(fis);
                DatabaseConfig config = new DatabaseConfig(props);
                StorageFactory factory = new StorageFactory(config);
                new UserLoadInitialDataCommand(factory.getStorage()).run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (args.length > 0 && args[0].equals("client")) {
            TCPIPClient.main(args);
            System.out.println("TCPIPClient !!!");
        } else {
            System.out.println("Usage: java -jar myapp.jar <server|client>");
        }        
    }
}

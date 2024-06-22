package sample;

import sample.command.UserLoadInitialDataCommand;
import sample.command.FetchUserCoinCommand;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.service.StorageFactory;
import com.scalar.db.api.DistributedStorage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        try {
            // Load database.properties file
            Properties props = new Properties();
            props.load(new FileInputStream("database.properties"));
            DatabaseConfig config = new DatabaseConfig(props);

            // Create StorageFactory
            StorageFactory factory = new StorageFactory(config);

            // Get DistributedStorage
            DistributedStorage storage = factory.getStorage();

            // Load initial data
            new UserLoadInitialDataCommand(storage).run();

            // Get user coin
            new FetchUserCoinCommand(storage, 1).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

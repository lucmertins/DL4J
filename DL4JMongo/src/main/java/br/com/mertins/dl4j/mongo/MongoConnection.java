package br.com.mertins.dl4j.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author mertins
 */
public class MongoConnection {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public void doConnectionMongo(Properties properties, boolean auth) {
        String server = ((String) properties.get("nosqlServer")).trim();
        String port = ((String) properties.get("nosqlPort")).trim();
        String database = ((String) properties.get("nosqlDatabase")).trim();
        String user = ((String) properties.get("nosqlUser")).trim();
        String passwd = ((String) properties.get("nosqlPasswd")).trim();
        List<ServerAddress> serverAddresses = new ArrayList<>();
        serverAddresses.add(new ServerAddress(server, Integer.valueOf(port)));
        if (auth) {
            List<MongoCredential> credentials = new ArrayList<>();
            credentials.add(MongoCredential.createCredential(user, database, passwd.toCharArray()));
        }
        mongoClient = new MongoClient(serverAddresses);
        mongoDatabase = mongoClient.getDatabase(database);

    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public void close() {
        try {
            mongoClient.close();
        } catch (Exception ex) {
        }
        mongoClient = null;
        mongoDatabase = null;

    }
}

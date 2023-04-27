package org.kendar.mongo;

import ch.qos.logback.classic.Level;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;

import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AgainstRealHamTest {
    private static final boolean DO_RUN = false;

    @BeforeEach
    void beforeEach() {
        var loggerBuilder = (LoggerBuilder) new LocalLoggerBuilderImpl();
        Logger logger = loggerBuilder.build(AgainstRealHamTest.class);
        loggerBuilder.setLevel("org.mongodb.driver", Level.OFF);
    }

    @Test
    void test_ping_on_real_mongo() {
        if (!DO_RUN) return;
        String uri = "mongodb://127.0.0.1:27097";///?maxPoolSize=20&w=majority";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                assertEquals(1.0, commandResult.get("ok"));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException me) {
                me.printStackTrace();
            }
        }
    }

    @Test
    void test_insert_select_on_real_mongo() {
        if (!DO_RUN) return;
        String uri = "mongodb://127.0.0.1:27097";///?maxPoolSize=1&w=majority";
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            Sleeper.sleep(1000);
            MongoDatabase database = mongoClient.getDatabase("basicdb");
            Sleeper.sleep(1000);
            MongoCollection<Document> collection = database.getCollection("movies");
            try {
                Sleeper.sleep(1000);
                InsertOneResult result = collection.insertOne(new Document()
                        .append("_id", new ObjectId())
                        .append("title", "Ski Bloopers")
                        .append("genres", Arrays.asList("Documentary", "Comedy")));
                System.out.println("Success! Inserted document id: " + result.getInsertedId());
                Sleeper.sleep(1000);
                Document doc = collection.find(eq("title", "Ski Bloopers"))
                        .first();
                assertNotNull(doc);
                System.out.println(doc);
            } catch (MongoException me) {
                me.printStackTrace();
            }
        }
    }

    @Test
    void test_stats__real_mongo() {
        if (!DO_RUN) return;
        String uri = "mongodb://127.0.0.1:27097";///?maxPoolSize=1&w=majority";
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            Sleeper.sleep(1000);
            MongoDatabase database = mongoClient.getDatabase("basicdb");
            Sleeper.sleep(1000);

            Bson command = new BsonDocument("dbStats", new BsonInt64(1));
            Document commandResult = database.runCommand(command);
            assertNotNull(commandResult);
            System.out.println("dbStats: " + commandResult.toJson());
        }
    }

    @Test
    void test_hostInfo_real_mongo() {
        if (!DO_RUN) return;
        String uri = "mongodb://127.0.0.1:27097";///?maxPoolSize=1&w=majority";
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            Sleeper.sleep(1000);
            MongoDatabase database = mongoClient.getDatabase("basicdb");
            Sleeper.sleep(1000);

            Bson command = new BsonDocument("hostInfo", new BsonInt64(1));
            Document commandResult = database.runCommand(command);
            assertNotNull(commandResult);
            System.out.println("hostInfo: " + commandResult.toJson());
        }
    }

    @Test
    void test_db_real_mongo() {
        if (!DO_RUN) return;
        String uri = "mongodb://127.0.0.1:27097";///?maxPoolSize=1&w=majority";
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            Sleeper.sleep(1000);
            MongoDatabase database = mongoClient.getDatabase("basicdb");
            assertNotNull(database);
        }
    }
}

package org.kendar.mongo;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kendar.utils.Sleeper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MongoTest {

    private Thread subClientThread;
    private ServerSocket server;
    private Thread clientThread;

    public void server(int port) throws IOException {
        server = new ServerSocket(port);
        while(true) {
            try {
                Socket client = server.accept();
                subClientThread = new Thread(new MongoClientHandler(client));
                subClientThread.start();
            }catch (SocketException se){

            }
        }
    }

    @BeforeEach
    void beforeEach() {
        clientThread = new Thread(()->{
            try {
                server(27917);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();
        Sleeper.sleep(1000);
    }

    @AfterEach
    void afterEach(){
        try {
            server.close();
        } catch (IOException e) {

        }
        subClientThread.interrupt();
        clientThread.interrupt();
    }

    @Test
    void test_ping_on_real_mongo() {
        String uri = "mongodb://127.0.0.1:27917/?maxPoolSize=20&w=majority";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                assertEquals(1.0,commandResult.get("ok"));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException me) {
                me.printStackTrace();
            }
        }
    }

    @Test
    void test_insert_select_on_real_mongo() {
        String uri = "mongodb://127.0.0.1:27917/?maxPoolSize=1&w=majority";
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            MongoCollection<Document> collection = database.getCollection("movies");
            try {
                InsertOneResult result = collection.insertOne(new Document()
                        .append("_id", new ObjectId())
                        .append("title", "Ski Bloopers")
                        .append("genres", Arrays.asList("Documentary", "Comedy")));
                System.out.println("Success! Inserted document id: " + result.getInsertedId());
                Document doc = collection.find(eq("title", "Ski Bloopers"))
                        .first();
                System.out.println(doc);
            } catch (MongoException me) {
                me.printStackTrace();
            }
        }
    }
}

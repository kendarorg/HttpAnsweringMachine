package org.kendar.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
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
import org.junit.jupiter.api.Test;
import org.kendar.utils.Sleeper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;

public class MongoTest {

    private MongoClientHandler clh;

    public void server(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        while(true) {
            Socket client = server.accept();
            Thread clientThread = new Thread(new MongoClientHandler(client));
            clientThread.start();
        }
    }

    /*@Test
    void doTest() throws IOException, InterruptedException {
        Thread clientThread = new Thread(()->{
            try {
                server(27917);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();
        Thread.sleep(100);
        String uri = "mongodb://user:pass@127.0.0.1:27917/?maxPoolSize=20&w=majority";
        // Construct a ServerApi instance using the ServerApi.builder() method

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                //.serverApi(serverApi)
                .build();

        MongoClientURI connectionString = new MongoClientURI(uri);
        // Create a new client and connect to the server
        try (MongoClient mongoClient = new MongoClient(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException me) {
                System.err.println(me);
            }
        }
    }*/

    @Test
    void doTest2() throws IOException, InterruptedException {
        Thread clientThread = new Thread(()->{
            try {
                server(27917);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();
        Thread.sleep(100);
        String uri = "mongodb://127.0.0.1:27917/?maxPoolSize=20&w=majority";
        // Construct a ServerApi instance using the ServerApi.builder() method
        /*ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();

        MongoClientURI connectionString = new MongoClientURI(uri);*/
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException me) {
                System.err.println(me);
            }
        }
        Thread.sleep(1000);
    }

    @Test
    void doTest3() throws IOException, InterruptedException {
        Thread clientThread = new Thread(()->{
            try {
                server(27917);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();
        Thread.sleep(100);
        String uri = "mongodb://127.0.0.1:27917/?maxPoolSize=1&w=majority";
        // Construct a ServerApi instance using the ServerApi.builder() method
        /*ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();

        MongoClientURI connectionString = new MongoClientURI(uri);*/
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            var res = mongoClient.listDatabaseNames().first();
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
                System.err.println("Unable to insert due to an error: " + me);
            }
        }
        clientThread.interrupt();
    }
}

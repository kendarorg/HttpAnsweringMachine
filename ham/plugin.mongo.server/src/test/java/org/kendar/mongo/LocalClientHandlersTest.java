package org.kendar.mongo;

import ch.qos.logback.classic.Level;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kendar.mongo.compressor.NoopCompressionHandler;
import org.kendar.mongo.compressor.SnappyCompressionHandler;
import org.kendar.mongo.compressor.ZStdCompressionHandler;
import org.kendar.mongo.compressor.ZlibCompressionHandler;
import org.kendar.mongo.config.MongoDescriptor;
import org.kendar.mongo.config.MongoProxy;
import org.kendar.mongo.handlers.*;
import org.kendar.mongo.logging.MongoLogClient;
import org.kendar.mongo.logging.MongoLogServer;
import org.kendar.mongo.responder.*;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LocalClientHandlersTest {
    private static final boolean USE_JSON=true;

    private Thread subClientThread;
    private ServerSocket server;
    private Thread clientThread;
    private String targetIp;
    private int targetPort;
    private String connectionString;
    private MongoServer mongoServer;

    public void server(int port,boolean useJson) throws IOException {

        var loggerBuilder = (LoggerBuilder)new LocalLoggerBuilderImpl();
        server = new ServerSocket(port);
        var responders = (List<MongoResponder>)List.of(
                new OpMsgResponder(loggerBuilder), new OpMsgMasterResponder(loggerBuilder),
                new OpQueryResponder(),new OpQueryMasterResponder()
        );

        var msgHandlers = (List<MsgHandler>)List.of(
          new OpDeleteHandler(), new OpInsertHandler(),
          new OpMsgHandler(), new OpQueryHandler(), new OpReplyHandler(), new OpUpdateHandler()
        );
        var compressionHandlers = List.of(
                new NoopCompressionHandler(), new SnappyCompressionHandler(),
                new ZlibCompressionHandler(), new ZStdCompressionHandler()
        );
        Logger logger = loggerBuilder.build(LocalClientHandlersTest.class);
        loggerBuilder.setLevel("org.mongodb.driver", Level.OFF);
        while(true) {
            try {
                Socket client = server.accept();
                logger.debug("++++++++++++++ACCEPTED CONNECTION");
                if(useJson) {
                    var proxy = new MongoProxy();
                    proxy.setExposedPort(27097);
                    MongoDescriptor md = new MongoDescriptor();
                    md.setConnectionString("mongodb://127.0.0.1:27017");
                    proxy.setRemote(md);
                    var handler = new JsonMongoClientHandler(client,proxy, msgHandlers, compressionHandlers, loggerBuilder,responders);
                    subClientThread = new Thread(handler);
                    subClientThread.start();
                }else {
                    var handler = new DirectMongoClientHandler(client, msgHandlers, compressionHandlers, loggerBuilder);
                    handler.setTarget(targetIp, targetPort);
                    subClientThread = new Thread(handler);
                    subClientThread.start();
                }
            }catch (SocketException se){

            }
        }
    }

    @BeforeEach
    void beforeEach() {
        var loggerBuilder = (LoggerBuilder)new LocalLoggerBuilderImpl();
        Logger logger = loggerBuilder.build(LocalClientHandlersTest.class);
        loggerBuilder.setLevel("org.mongodb.driver", Level.OFF);
        loggerBuilder.setLevel("io.netty", Level.OFF);
        loggerBuilder.setLevel("de.bwaldvogel.mongo", Level.OFF);
        loggerBuilder.setLevel(MongoLogClient.class.getName(), Level.DEBUG);
        loggerBuilder.setLevel(MongoLogServer.class.getName(), Level.DEBUG);
        mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("127.0.0.1",27017);
        targetIp = "127.0.0.1";
        targetPort = 27017;
    }

    private void startServer(boolean useJson) {
        clientThread = new Thread(()->{
            try {
                server(27097,useJson);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();
        Sleeper.sleep(1000);
    }

    @AfterEach
    void afterEach(){
        mongoServer.shutdown();
        try {
            server.close();
        } catch (IOException e) {

        }
        if(subClientThread!=null) {
            subClientThread.interrupt();
        }
        clientThread.interrupt();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    void test_ping_on_real_mongo(boolean useJsonServer) {

        startServer(useJsonServer);
        String uri = "mongodb://127.0.0.1:27097";///?maxPoolSize=20&w=majority";
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

    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    void test_insert_select_on_real_mongo(boolean useJsonServer) {

        startServer(useJsonServer);
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
                System.out.println("RETURNED :"+doc.toJson());
            } catch (MongoException me) {
                me.printStackTrace();
            }
        }
    }


    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    void test_stats__real_mongo(boolean useJsonServer) {

        startServer(useJsonServer);
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

    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    void test_db_real_mongo(boolean useJsonServer) {

        startServer(useJsonServer);
        String uri = "mongodb://127.0.0.1:27097";///?maxPoolSize=1&w=majority";
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            Sleeper.sleep(1000);
            MongoDatabase database = mongoClient.getDatabase("basicdb");
            assertNotNull(database);
        }
    }
}

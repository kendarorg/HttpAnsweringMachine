package org.kendar;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class Main {
    public static int MONGO_PORT = 27077;

    public static void main(String[] args){
        var mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("127.0.0.1", MONGO_PORT);
    }
}

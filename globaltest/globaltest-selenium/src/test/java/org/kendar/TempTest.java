package org.kendar;

import org.junit.jupiter.api.Test;
import org.kendar.utils.Sleeper;

import static org.kendar.MongoRecordingSetupTest.startMongo;

public class TempTest {
    @Test
    void test(){
        startMongo();
        Sleeper.sleep(100000000);
    }
}

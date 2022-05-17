package org.kendar.ham;

import org.junit.jupiter.api.BeforeAll;
import org.kendar.ham.GlobalSettings;
import org.kendar.ham.HamException;
import org.kendar.ham.HamStarter;
import org.kendar.ham.HamTestException;

public class HamReplayerIT {

    @BeforeAll
    public static void beforeAll() throws  HamTestException {
        HamStarter.runHamJar();
    }
}

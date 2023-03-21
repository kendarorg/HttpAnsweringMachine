package org.kendar;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kendar.googlehack.GoogleHackSetupTest;

@ExtendWith({SeleniumBase.class})
public class SeleniumTest extends SeleniumBase{



    @Test
    void googleHack() throws Throwable {
        var driver = SeleniumBase.getDriver();
        GoogleHackSetupTest.setup(driver);
        restart();
        driver = SeleniumBase.getDriver();
        GoogleHackSetupTest.verify(driver);
        close();
    }
}

package org.kendar;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SeleniumBase.class})
public class SeleniumTest extends SeleniumBase{
    @BeforeAll

    @Test
    void aTest() throws InterruptedException {
        var driver = SeleniumBase.getDriver();
        driver.get("https://www.google.com");
        Thread.sleep(1000);
    }
}

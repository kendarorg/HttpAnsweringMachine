package org.kendar.ham;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tkendar.ham.HamStarter;
import org.tkendar.ham.HamTestException;

import static org.junit.jupiter.api.Assertions.*;

public class ExtraTests {
    @BeforeAll
    public static void beforeAll() throws HamTestException {
        HamStarter.runHamJar(DnsTest.class);
    }
    private HamBasicBuilder hamBuilder = GlobalSettings.builder();

    @Test
    void testSwagger() throws HamException {
        var request = ((HamBuilder)hamBuilder).newRequest()
                .withMethod("GET")
                .withPath("/api/swagger/map.json");
        var response = hamBuilder.call(request.build());
        assertTrue(response.getResponseText().contains("\"3.0.1\""));
        assertTrue(response.getResponseText().contains("\"openapi\""));
    }

    @Test
    void testHealth() throws HamException {
        var request = ((HamBuilder)hamBuilder).newRequest()
                .withMethod("GET")
                .withPath("/api/health");
        var response = hamBuilder.call(request.build());
        assertTrue(response.getResponseText().equalsIgnoreCase("OK"));
    }

    @Test
    void testDownloadSetting() throws HamException {
        var request = ((HamBuilder)hamBuilder).newRequest()
                .withMethod("GET")
                .withPath("/api/utils/settings");
        var response = hamBuilder.call(request.build());
        assertTrue(response.getResponseText().contains("jdbc:h2:tcp://localhost/ham;MODE=MYSQL;"));
    }
}

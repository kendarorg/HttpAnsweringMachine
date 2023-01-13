package org.kendar.ham;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProxiesTest {
    @BeforeAll
    public static void beforeAll() throws HamTestException {
        HamStarter.runHamJar(ProxiesTest.class);
    }
    private HamBasicBuilder hamBuilder = GlobalSettings.builder();
    @Test
    public void testAddingProxy() throws HamException, InterruptedException {
        var proxyId = hamBuilder
                .proxies()
                .addProxy("http://www.microsoft.com","http://www.local.test/api/health","www.local.test:80");

        assertNotNull(proxyId);
        hamBuilder.proxies().removeProxy(proxyId);
    }

    @Test
    public void testAddingDbProxy() throws HamException, InterruptedException {
        var connectionString = hamBuilder
                .proxies()
                .addRemoteDbProxy("jdbc:test","login","password","org.test.JdbcDriver")
                .asInactive()
                .asLocal("exposed","login","password");

        assertEquals("jdbc:janus:http://www.local.test/api/db/exposed",connectionString);
        hamBuilder.proxies().removeDbProxy("exposed");
    }
}

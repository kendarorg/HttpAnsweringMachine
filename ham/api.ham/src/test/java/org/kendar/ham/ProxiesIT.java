package org.kendar.ham;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProxiesIT {
    @Test
    public void testAddingProxy() throws HamException, InterruptedException {
        var proxyId = GlobalSettings.builder()
                .proxies()
                .addProxy("http://www.microsoft.com","http://www.local.test/api/health","www.local.test:80");

        assertNotNull(proxyId);
    }
}
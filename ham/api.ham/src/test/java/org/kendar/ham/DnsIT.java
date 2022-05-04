package org.kendar.ham;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DnsIT {
    @Test
    public void testAddingDns() throws HamException {
        var dnsId = GlobalSettings.builder()
                .dns()
                .addDnsName("10.1.0.1", "test.dns0");
        assertNotNull(dnsId);
    }

    @Test
    public void testAddingDnsServer() throws HamException {
        var serverId = GlobalSettings.builder()
                .dns()
                .addDnsServer("10.1.0.1", false);
        assertNotNull(serverId);
    }


    @Test
    public void testResolved() throws HamException {
        var serverId = GlobalSettings.builder()
                .dns()
                .resolve("www.google.com");
        assertNotNull(serverId);

        var resolved = GlobalSettings.builder()
                .dns()
                .retrieveResolvedNames();
        assertNotNull(resolved);
        assertTrue(resolved.size()>0);
        assertTrue(resolved.stream().anyMatch(r->r.getName().equalsIgnoreCase("www.google.com")));
    }
}


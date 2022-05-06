package org.kendar.ham;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DnsIT {
    private HamBasicBuilder hamBuilder = GlobalSettings.builder();
    @Test
    public void testAddingDns() throws HamException {
        var dnsId =hamBuilder
                .dns()
                .addDnsName("10.1.0.1", "test.dns0");
        assertNotNull(dnsId);
        hamBuilder.dns().removeDnsName(dnsId);
    }

    @Test
    public void testAddingDnsServer() throws HamException {
        var serverId = hamBuilder
                .dns()
                .addDnsServer("10.1.0.1", false);
        assertNotNull(serverId);
        hamBuilder.dns().removeDnsServer(serverId);
    }


    @Test
    public void testResolved() throws HamException {
        var serverId = hamBuilder
                .dns()
                .resolve("www.google.com");
        assertNotNull(serverId);

        var resolved = hamBuilder
                .dns()
                .retrieveResolvedNames();
        assertNotNull(resolved);
        assertTrue(resolved.size()>0);
        assertTrue(resolved.stream().anyMatch(r->r.getName().equalsIgnoreCase("www.google.com")));
    }
}


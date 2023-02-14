package org.kendar.ham;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tkendar.ham.HamStarter;
import org.tkendar.ham.HamTestException;

import static org.junit.jupiter.api.Assertions.*;

public class DnsTest {
    @BeforeAll
    public static void beforeAll() throws HamTestException {
        HamStarter.runHamJar(DnsTest.class);
    }
    private HamBasicBuilder hamBuilder = GlobalSettings.builder();

    @Test
    public void testAddingDnsFix() throws HamException {
        final String WEIRD_NAME = "casper.core-workload.qa.aws.lmn";
        final String IP = "10.0.0.1";
        var dnsId =hamBuilder
                .dns()
                .addDnsName(IP, WEIRD_NAME);
        var resolved = hamBuilder.dns().resolve(WEIRD_NAME);
        assertNotNull(resolved);
        assertEquals(IP,resolved);
        hamBuilder.dns().removeDnsName(dnsId);
    }


    @Test
    public void testNamedDns() throws HamException {

        var dnsId1 =hamBuilder
                .dns()
                .addDnsName("10.0.0.1", "test.com");

        var dnsId2 =hamBuilder
                .dns()
                .addDnsName("test.com", "prova.com");
        var resolved = hamBuilder.dns().resolve("prova.com");
        assertNotNull(resolved);
        assertEquals("10.0.0.1",resolved);
        hamBuilder.dns().removeDnsName(dnsId1);
        hamBuilder.dns().removeDnsName(dnsId2);
    }

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


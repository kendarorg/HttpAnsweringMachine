package org.kendar.ham;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DnsIT {
    @Test
    public void testAddingDns() throws HamException {
        var dnsName = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1",1080)
                .withDns("127.0.0.1")
                .dns()
                .addDnsName("10.1.0.1", "test.dns0");
        assertNotNull(dnsName);
    }
}


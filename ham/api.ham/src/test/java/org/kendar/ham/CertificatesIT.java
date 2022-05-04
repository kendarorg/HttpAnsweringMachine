package org.kendar.ham;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CertificatesIT {
    @Test
    public void testAddingCertificate() throws HamException, InterruptedException {
        GlobalSettings.builder()
                .certificates()
                .addAltName("www.google.com");
        Thread.sleep(1000);
        var names = GlobalSettings.builder()
                .certificates()
                .retrieveAltNames();
        assertNotNull(names);
        assertTrue(names.size()>0);
        assertTrue(names.stream().anyMatch(r->r.getAddress().equalsIgnoreCase("www.google.com")));
    }
}

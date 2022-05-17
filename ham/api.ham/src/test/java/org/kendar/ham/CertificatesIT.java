package org.kendar.ham;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CertificatesIT {
    @BeforeAll
    public static void beforeAll() throws HamTestException {
        HamStarter.runHamJar();
    }
    private HamBasicBuilder hamBuilder = GlobalSettings.builder();
    @Test
    public void testAddingCertificate() throws HamException, InterruptedException {
        var inserted = hamBuilder.certificates()
                .addAltName("www.google.com");
        assertNotNull(inserted);
        assertTrue(inserted.size()==1);
        var newItems = hamBuilder.certificates().retrieveAltNames().stream()
                        .filter(add->
                                add.getAddress().equalsIgnoreCase("www.google.com")
                                && add.getId().equalsIgnoreCase(inserted.get(0))
                        ).count();
        assertEquals(1,newItems);
        hamBuilder.certificates().removeAltName(inserted.get(0));
    }
}

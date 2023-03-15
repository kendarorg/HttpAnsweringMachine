package org.kendar.ham;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tkendar.ham.HamStarter;
import org.tkendar.ham.HamTestException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProxyDnsSslCheckTest {
    @BeforeAll
    public static void beforeAll() throws HamTestException {
        HamStarter.runHamJar(ProxyDnsSslCheckTest.class);
    }

    @Test
    public void verifyProxyDnsHttpsStuffs() throws HamException, IOException, InterruptedException {
        var hamCertificateNotInstalledOnJvm = true;
        var hamBuilder = (HamBuilder) GlobalSettings.builder();

        //Add dns
        var dnsNameId = hamBuilder.dns().addDnsName("127.0.0.1", "github.com");
        //Add proxy
        var proxyId = hamBuilder.proxies().addProxy(
                "https://github.com",
                "https://www.microsoft.com",
                "127.0.0.1");
        //Intercept the ceritficate
        var certificateId = hamBuilder.certificates().addAltName("github.com");

        Thread.sleep(500);


        HttpGet httpGet = new HttpGet("https://github.com");
        CloseableHttpResponse clientResponse = hamBuilder.execute(httpGet, hamCertificateNotInstalledOnJvm);
        String data = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        assertTrue(data.toLowerCase(Locale.ROOT).contains("microsoft"));
        assertFalse(data.toLowerCase(Locale.ROOT).contains("github"));

        hamBuilder.proxies().removeProxy(proxyId);

        var httpGet2 = new HttpGet("https://github.com");
        var clientResponse2 = hamBuilder.execute(httpGet2, hamCertificateNotInstalledOnJvm);
        var data2 = IOUtils.toString(clientResponse2.getEntity().getContent(), StandardCharsets.UTF_8);
        assertTrue(data2.toLowerCase(Locale.ROOT).contains("github"));
        assertFalse(data2.toLowerCase(Locale.ROOT).contains("microsoft"));

        hamBuilder.dns().removeDnsName(dnsNameId);
        hamBuilder.certificates().removeAltName(certificateId.get(0));
    }
}

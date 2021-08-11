package org.kendar.servers.certificates;


import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class GeneratedCert {
    public final PrivateKey privateKey;
    public final X509Certificate certificate;

    public GeneratedCert(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }
}

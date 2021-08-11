package org.kendar.servers.certificates;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface CertificatesManager {
    GeneratedCert getCaCertificate();
    GeneratedCert loadRootCertificate(String derFile, String keyFile) throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException;
    GeneratedCert createCertificate(String cnName, String rootDomain, GeneratedCert issuer, List<String> childDomains, boolean isCA) throws Exception;
}

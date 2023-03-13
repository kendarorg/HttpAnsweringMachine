package org.kendar.servers.certificates;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

public interface CertificatesManager {
    GeneratedCert getCaCertificate();

    GeneratedCert loadRootCertificate(String derFile, String keyFile) throws CertificateException, IOException;

    GeneratedCert createCertificate(String cnName, String rootDomain, GeneratedCert issuer, List<String> childDomains, boolean isCA) throws Exception;
}

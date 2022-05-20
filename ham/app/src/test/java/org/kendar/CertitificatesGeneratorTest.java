package org.kendar;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kendar.servers.certificates.CertificatesManagerImpl;
import org.kendar.servers.certificates.GeneratedCert;
import org.kendar.utils.FileResourcesUtilsImpl;
import org.kendar.utils.LoggerBuilderImpl;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CertitificatesGeneratorTest {
  @Test
  @Disabled
  public void generateFromScratch() throws Exception {
    var loggerBuilder = new LoggerBuilderImpl();
    var target = new CertificatesManagerImpl(null, loggerBuilder);
    var extraDomains = new ArrayList<String>();
    GeneratedCert rootCA =
        target.createCertificate("CN=do_not_trust_test_certs_root", null, null, extraDomains, true);
    GeneratedCert issuer =
        target.createCertificate(
            "CN=do_not_trust_test_certs_issuer", null, rootCA, extraDomains, true);
    GeneratedCert domain =
        target.createCertificate(
            "CN=local.gamlor.info", "local.gamlor.info", issuer, extraDomains, false);
    GeneratedCert otherD =
        target.createCertificate(
            "CN=other.gamlor.info", "other.gamlor.info", issuer, extraDomains, false);
  }

  @Test
  public void loadRootCertificate() throws Exception {
    var loggerBuilder = new LoggerBuilderImpl();
    var resourcesLoader = new FileResourcesUtilsImpl(loggerBuilder);
    var target = new CertificatesManagerImpl(resourcesLoader, loggerBuilder);
    var root = target.loadRootCertificate("certificates/ca.der", "certificates/ca.key");
  }

  @Test
  public void generateFromRoot() throws Exception {
    var loggerBuilder = new LoggerBuilderImpl();
    var resourcesLoader = new FileResourcesUtilsImpl(loggerBuilder);
    var target = new CertificatesManagerImpl(resourcesLoader, loggerBuilder);
    var root = target.loadRootCertificate("certificates/ca.der", "certificates/ca.key");
    var extraDomains = new ArrayList<String>();
    GeneratedCert domain =
        target.createCertificate(
            "CN=local.gamlor.info", "local.gamlor.info", root, extraDomains, false);
    var encodedBytes = domain.certificate.getEncoded();
    final FileOutputStream os = new FileOutputStream("target/local.gamlor.info.cer");
    os.write("-----BEGIN CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.write(Base64.encodeBase64(encodedBytes, true));
    os.write("-----END CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.close();
  }

  @Test
  public void exportCertificate() throws Exception {
    var loggerBuilder = new LoggerBuilderImpl();
    var resourcesLoader = new FileResourcesUtilsImpl(loggerBuilder);
    var target = new CertificatesManagerImpl(resourcesLoader, loggerBuilder);
    var root = target.loadRootCertificate("certificates/ca.der", "certificates/ca.key");
    var encodedBytes = root.certificate.getEncoded();

    final FileOutputStream os = new FileOutputStream("target/cert.cer");
    os.write("-----BEGIN CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.write(Base64.encodeBase64(encodedBytes, true));
    os.write("-----END CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.close();
  }

  @Test
  public void generateMultiple() throws Exception {
    var loggerBuilder = new LoggerBuilderImpl();
    var resourcesLoader = new FileResourcesUtilsImpl(loggerBuilder);
    var target = new CertificatesManagerImpl(resourcesLoader, loggerBuilder);
    var root = target.loadRootCertificate("certificates/ca.der", "certificates/ca.key");
    var extraDomains = new ArrayList<String>();
    extraDomains.add("*.eu-west-1.tsaws.kendar.org");
    extraDomains.add("*.tsint.kendar.org");
    extraDomains.add("*.kendar.org");
    extraDomains.add("kendar.org");
    GeneratedCert domain =
        target.createCertificate(
            "CN=kendar.org,O=Local Development, C=US", null, root, extraDomains, false);
    var encodedBytes = domain.certificate.getEncoded();

    final FileOutputStream os = new FileOutputStream("target/kendar.org.cer");
    os.write("-----BEGIN CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.write(Base64.encodeBase64(encodedBytes, true));
    os.write("-----END CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.close();
  }

  @Test
  public void generateMultiple2() throws Exception {
    var loggerBuilder = new LoggerBuilderImpl();
    var resourcesLoader = new FileResourcesUtilsImpl(loggerBuilder);
    var target = new CertificatesManagerImpl(resourcesLoader, loggerBuilder);
    var root = target.loadRootCertificate("certificates/ca.der", "certificates/ca.key");
    var extraDomains = new ArrayList<String>();
    extraDomains.add("kendar.org");
    extraDomains.add("*.kendar.org");
    GeneratedCert domain =
        target.createCertificate(
            "C=US,O=Local Development,CN=kendar.org", null, root, extraDomains, false);
    var encodedBytes = domain.certificate.getEncoded();

    final FileOutputStream os = new FileOutputStream("target/kendar.org2.cer");
    os.write("-----BEGIN CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.write(Base64.encodeBase64(encodedBytes, true));
    os.write("-----END CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    os.close();
  }
}

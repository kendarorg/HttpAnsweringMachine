package org.kendar.servers.certificates;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class CertificatesManagerImpl implements CertificatesManager {
  private final Logger logger;
  private final FileResourcesUtils fileResourcesUtils;
  private GeneratedCert caCertificate;

  public CertificatesManagerImpl(
      FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
    this.fileResourcesUtils = fileResourcesUtils;
    this.logger = loggerBuilder.build(CertificatesManagerImpl.class);
    Provider aProvider = Security.getProvider("BC");
    if (aProvider == null) {
      updateProvider();
    }
  }

  public void updateProvider() {
    //Security.insertProviderAt(new BouncyCastleProvider(), 1);
    Security.addProvider(new BouncyCastleProvider());
  }

  @Override
  public GeneratedCert getCaCertificate() {
    return caCertificate;
  }

  @Override
  public GeneratedCert loadRootCertificate(String derFile, String keyFile)
      throws CertificateException, IOException {
    var caStream = fileResourcesUtils.getFileFromResourceAsStream(derFile);
    var certificateFactory = CertificateFactory.getInstance("X.509");
    var certificate = (X509Certificate) certificateFactory.generateCertificate(caStream);

    var keyStream = fileResourcesUtils.getFileFromResourceAsStream(keyFile);

    PEMParser pemParser = new PEMParser(new InputStreamReader(keyStream));
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    Object object = pemParser.readObject();
    KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
    var privateKey = kp.getPrivate();

    caCertificate = new GeneratedCert(privateKey, certificate);
    return caCertificate;
  }

  @Override
  public GeneratedCert createCertificate(
      String cnName,
      String rootDomain,
      GeneratedCert issuer,
      List<String> childDomains,
      boolean isCA)
      throws Exception {

    logger.trace("Create certificate");
    // Generate the key-pair with the official Java API's
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    KeyPair certKeyPair = keyGen.generateKeyPair();
    X500Name name = new X500Name(cnName);

    // If you issue more than just test certificates, you might want a decent serial number schema
    // ^.^
    BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
    Instant now = Instant.now();
    Instant validFrom = now.minus(360, ChronoUnit.DAYS);
    Instant validUntil = now.plus(10 * 360, ChronoUnit.DAYS);

    // If there is no issuer, we self-sign our certificate.
    X500Name issuerName;
    PrivateKey issuerKey;
    if (issuer == null) {
      issuerName = name;
      issuerKey = certKeyPair.getPrivate();
    } else {
      issuerName = new X500Name(issuer.certificate.getSubjectDN().getName());
      issuerKey = issuer.privateKey;
    }

    // The cert builder to build up our certificate information
    JcaX509v3CertificateBuilder builder =
        new JcaX509v3CertificateBuilder(
            issuerName,
            serialNumber,
            Date.from(validFrom),
            Date.from(validUntil),
            name,
            certKeyPair.getPublic());

    // Make the cert to a Cert Authority to sign more certs when needed
    if (isCA) {
      builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
    }
    // Modern browsers demand the DNS name entry
    if (rootDomain != null) {
      builder.addExtension(
          Extension.subjectAlternativeName,
          false,
          new GeneralNames(new GeneralName(GeneralName.dNSName, rootDomain)));
    } else if (childDomains.size() > 0) {
      var generalNames = new GeneralName[childDomains.size()];
      for (int i = 0; i < childDomains.size(); i++) {
        generalNames[i] = new GeneralName(GeneralName.dNSName, childDomains.get(i));
      }

      // GeneralNames subjectAltNames = GeneralNames.getInstance(generalNames);
      builder.addExtension(Extension.subjectAlternativeName, false, new GeneralNames(generalNames));
    }
    if (!isCA && issuer != null) {
      byte[] extvalue =
          issuer.certificate.getExtensionValue(Extension.authorityKeyIdentifier.getId());
      if (extvalue != null) {
        byte[] filteredByteArray =
            Arrays.copyOfRange(extvalue, extvalue.length - 20, extvalue.length);

        AuthorityKeyIdentifier authorityKeyIdentifier =
            new AuthorityKeyIdentifier(filteredByteArray);
        builder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier);
      }
      builder.addExtension(
          new ASN1ObjectIdentifier("2.5.29.19"), false, new BasicConstraints(false));
      builder.addExtension(
          Extension.extendedKeyUsage,
          false,
          new ExtendedKeyUsage(
              new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth}));

      builder.addExtension(
          Extension.keyUsage,
          false,
          new X509KeyUsage(
              X509KeyUsage.digitalSignature
                  | X509KeyUsage.nonRepudiation
                  | X509KeyUsage.keyEncipherment
                  | X509KeyUsage.dataEncipherment));
    }

    // Finally, sign the certificate:
    ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuerKey);
    //ContentSigner signer = new JcaContentSignerBuilder("SHA256").build(issuerKey);
    X509CertificateHolder certHolder = builder.build(signer);
    X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

    return new GeneratedCert(certKeyPair.getPrivate(), cert);
  }
}

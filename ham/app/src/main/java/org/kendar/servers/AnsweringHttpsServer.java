package org.kendar.servers;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.kendar.events.EventQueue;
import org.kendar.events.events.SSLChangedEvent;
import org.kendar.servers.certificates.CertificatesManager;
import org.kendar.servers.certificates.GeneratedCert;
import org.kendar.servers.config.HttpsWebServerConfig;
import org.kendar.servers.config.SSLConfig;
import org.kendar.servers.config.SSLDomain;
import org.kendar.servers.config.WebServerConfig;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Inspired by
 * <a href="https://stackoverflow.com/questions/67720003/tls-1-3-server-socket-with-java-11-and-self-signed-certificates">https://stackoverflow.com/questions/67720003/tls-1-3-server-socket-with-java-11-and-self-signed-certificates</a>
 */
@Component
public class AnsweringHttpsServer implements AnsweringServer {
  private final JsonConfiguration configuration;
  private final Logger logger;
  private final AnsweringHandler handler;
  private final CertificatesManager certificatesManager;
  private final AtomicLong sslTimestamp = new AtomicLong(0);
  private final AtomicReference<SSLContext> sslSharedContext = new AtomicReference<>();
  private boolean running = false;
  private HttpsServer httpsServer;

  public AnsweringHttpsServer(
          LoggerBuilder loggerBuilder,
          AnsweringHandler handler,
          CertificatesManager certificatesManager,
          JsonConfiguration configuration,
          EventQueue eventQueue) {
    this.logger = loggerBuilder.build(AnsweringHttpsServer.class);
    this.handler = handler;
    this.certificatesManager = certificatesManager;
    this.configuration = configuration;
    eventQueue.register((e)->handleCertificateChange(e), SSLChangedEvent.class);
  }

  boolean restart = false;
  public void handleCertificateChange(SSLChangedEvent t) {
    restart =true;
  }

  public void isSystem() {}

  @Override
  public void run() {
    var config = configuration.getConfiguration(HttpsWebServerConfig.class).copy();
    if (running) return;
    if (!config.isActive()) return;
    running = true;

    try {

      // setup the socket address
      InetSocketAddress address = new InetSocketAddress(config.getPort());

      // initialise the HTTPS server
      setupHttpsServer(config, address);

      httpsServer.start();
      logger.info("Https server LOADED, port: " + config.getPort());
      var localConfig = configuration.getConfiguration(HttpsWebServerConfig.class);
      while (running && localConfig.isActive()) {
        Thread.sleep(1000);
        if(restart)break;
      }
      //if(executor!=null)executor.shutdownNow();
      httpsServer.stop(0);
      restart = false;


    } catch (Exception ex) {
      logger.error(
              "Failed to create HTTPS server on port " + config.getPort() + " of localhost", ex);
    } finally {
      running = false;
    }
  }

  private void setupHttpsServer(WebServerConfig config, InetSocketAddress address) throws Exception {
    httpsServer = HttpsServer.create(address, config.getBacklog());
    final SSLContext sslContextInt =
            getSslContext(configuration.getConfiguration(SSLConfig.class));

    var sslConfigTimestamp = configuration.getConfigurationTimestamp(SSLConfig.class);
    sslTimestamp.set(sslConfigTimestamp);
    sslSharedContext.set(sslContextInt);
    setupSll(sslContextInt);
    httpsServer.createContext("/", handler);
    if (config.isUseCachedExecutor()) {
      ExecutorService executor = Executors.newCachedThreadPool();
      httpsServer.setExecutor(executor); // creates a cached
    } else {
      httpsServer.setExecutor(null); // creates a default executor
    }
  }

  private void setupSll(SSLContext sslContextInt) {
    httpsServer.setHttpsConfigurator(
            new HttpsConfigurator(sslContextInt) {
              public void configure(HttpsParameters params) {
                try {
                  // initialise the SSL context
                  SSLContext context = sslSharedContext.get();
                  var sslConfigTimestamp = configuration.getConfigurationTimestamp(SSLConfig.class);
                  if (sslConfigTimestamp > sslTimestamp.get()) {
                    var sslConfig = configuration.getConfiguration(SSLConfig.class);
                    sslTimestamp.set(sslConfigTimestamp);
                    context = getSslContext(sslConfig);
                    sslSharedContext.set(context);
                  }
                  SSLEngine engine = context.createSSLEngine();
                  params.setNeedClientAuth(false);
                  params.setCipherSuites(engine.getEnabledCipherSuites());
                  params.setProtocols(engine.getEnabledProtocols());

                  // Set the SSL parameters
                  SSLParameters sslParameters = context.getSupportedSSLParameters();
                  params.setSSLParameters(sslParameters);

                } catch (Exception ex) {
                  logger.debug("Failed to create HTTPS port");
                }
              }
            });
  }

  private SSLContext getSslContext(SSLConfig sslConfig) throws Exception {
    var root =
            certificatesManager.loadRootCertificate("certificates/ca.der", "certificates/ca.key");

    GeneratedCert domain =
            certificatesManager.createCertificate(
                    sslConfig.getCname(),
                    null,
                    root,
                    sslConfig.getDomains().stream().map(SSLDomain::getAddress).collect(Collectors.toList()),
                    false);

    KeyStore keyStoreTs = setupKeystore(domain);
    // now lets do the same with the keystore
    KeyStore keyStore = setupKeystore(domain);
    // HERE IS THE CHAIN
    X509Certificate[] chain = new X509Certificate[1];
    //chain[0] = root.certificate;
    chain[0] = domain.certificate;
    //chain[1] = root.certificate;
    keyStore.setKeyEntry("privateCert", domain.privateKey, "passphrase".toCharArray(), chain);

    TrustManagerFactory tmf =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(keyStoreTs);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, "passphrase".toCharArray());

    // create SSLContext to establish the secure connection
    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    logger.info("Https certificates generated");
    return ctx;
  }

  private KeyStore setupKeystore(GeneratedCert domain)
          throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore ksTemp = KeyStore.getInstance("jks"); //PKCS12
    ksTemp.load(null, null); // Initialize it
    ksTemp.setCertificateEntry("Alias", domain.certificate);
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    // save the temp keystore
    ksTemp.store(bOut, "passphrase".toCharArray());
    // Now create the keystore to be used by jsse
    KeyStore keyStore = KeyStore.getInstance("jks");
    keyStore.load(new ByteArrayInputStream(bOut.toByteArray()), "passphrase".toCharArray());
    return keyStore;
  }

  @Override
  public boolean shouldRun() {
    var localConfig = configuration.getConfiguration(HttpsWebServerConfig.class);
    return localConfig.isActive() && !running;
  }

  public void stop() {
    httpsServer.stop(0);
    running = false;

  }
}

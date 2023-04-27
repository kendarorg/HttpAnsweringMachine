package org.kendar.servers;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.kendar.events.EventQueue;
import org.kendar.events.ServiceStarted;
import org.kendar.events.events.SSLChangedEvent;
import org.kendar.servers.certificates.CertificatesManager;
import org.kendar.servers.certificates.GeneratedCert;
import org.kendar.servers.config.HttpsWebServerConfig;
import org.kendar.servers.config.SSLConfig;
import org.kendar.servers.config.SSLDomain;
import org.kendar.servers.config.WebServerConfig;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
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
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Inspired by
 * <a href="https://stackoverflow.com/questions/67720003/tls-1-3-server-socket-with-java-11-and-self-signed-certificates">https://stackoverflow.com/questions/67720003/tls-1-3-server-socket-with-java-11-and-self-signed-certificates</a>
 */
@Component
public class AnsweringHttpsServer implements AnsweringServer {
    public static final String PASSPHRASE = "passphrase";
    public static final String PRIVATE_CERT = "privateCert";
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final Logger logger;
    private final AnsweringHandler handler;
    private final CertificatesManager certificatesManager;
    private boolean running = false;
    private HashMap<String, HttpsServer> httpsServers = new HashMap<>();

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
        this.eventQueue = eventQueue;
        eventQueue.register(this::handleCertificateChange, SSLChangedEvent.class);
    }

    boolean restart = false;

    public void handleCertificateChange(SSLChangedEvent t) {
        restart = true;
    }

    public void isSystem() {
        //Marker for system classes
    }

    @Override
    public void run() {
        var config = configuration.getConfiguration(HttpsWebServerConfig.class).copy();
        if (running) return;
        if (!config.isActive()) return;
        running = true;
        httpsServers = new HashMap<>();

        try {
            var ports = config.getPort().split(";");
            for (var port : ports) {
                // setup the socket address
                InetSocketAddress address = new InetSocketAddress(Integer.parseInt(port));

                // initialise the HTTPS server
                var httpsServer = setupHttpsServer(config, address, port);

                httpsServer.start();

                eventQueue.handle(new ServiceStarted().withTye("https"));
                logger.info("Https server LOADED, port: {}", port);
            }
            var localConfig = configuration.getConfiguration(HttpsWebServerConfig.class);
            while (running && localConfig.isActive()) {
                Sleeper.sleep(1000);
                if (restart) break;
            }
            //if(executor!=null)executor.shutdownNow();
            for (var httpsServer : httpsServers.entrySet()) {
                httpsServer.getValue().stop(0);
            }
            restart = false;


        } catch (Exception ex) {
            logger.error(
                    "Failed to create HTTPS server on port " + config.getPort() + " of localhost", ex);
        } finally {
            running = false;
        }
    }

    private HttpsServer setupHttpsServer(WebServerConfig config, InetSocketAddress address, String port) throws Exception {
        var httpsServer = HttpsServer.create(address, config.getBacklog());

        httpsServers.put(port, httpsServer);
        final SSLContext sslContextInt =
                getSslContext(configuration.getConfiguration(SSLConfig.class));

        setupSll(sslContextInt, port);
        httpsServer.createContext("/", handler);
        if (config.isUseCachedExecutor()) {
            ExecutorService executor = Executors.newCachedThreadPool();
            httpsServer.setExecutor(executor); // creates a cached
        } else {
            httpsServer.setExecutor(null); // creates a default executor
        }
        return httpsServer;
    }

    private void setupSll(SSLContext sslContextInt, String port) {
        var context = sslContextInt;
        httpsServers.get(port).setHttpsConfigurator(
                new HttpsConfigurator(sslContextInt) {
                    @Override
                    public void configure(HttpsParameters params) {
                        try {
                            // initialise the SSL context

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
        chain[0] = domain.certificate;
        keyStore.setKeyEntry(PRIVATE_CERT, domain.privateKey, PASSPHRASE.toCharArray(), chain);

        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStoreTs);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, PASSPHRASE.toCharArray());

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
        ksTemp.store(bOut, PASSPHRASE.toCharArray());
        // Now create the keystore to be used by jsse
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(new ByteArrayInputStream(bOut.toByteArray()), PASSPHRASE.toCharArray());
        return keyStore;
    }

    @Override
    public boolean shouldRun() {
        var localConfig = configuration.getConfiguration(HttpsWebServerConfig.class);
        return localConfig.isActive() && !running;
    }

    public void stop() {
        for (var httpServer : httpsServers.entrySet()) {
            httpServer.getValue().stop(0);
        }
        running = false;

    }
}

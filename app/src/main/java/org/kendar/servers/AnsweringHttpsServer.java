package org.kendar.servers;

/**
 * https://stackoverflow.com/questions/67720003/tls-1-3-server-socket-with-java-11-and-self-signed-certificates
 */

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.kendar.servers.certificates.CertificatesManager;
import org.kendar.servers.certificates.GeneratedCert;
import org.kendar.servers.config.HttpWebServerConfig;
import org.kendar.servers.config.SSLConfig;
import org.kendar.servers.config.HttpsWebServerConfig;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class AnsweringHttpsServer  implements AnsweringServer{
    private final JsonConfiguration configuration;

    public void isSystem(){};
    private static final String SSL_CONTEXT_TYPE = "TLS";
    private static final String KEYSTORE_INSTANCE_TYPE = "JKS";
    private static final String CERTIFICATE_TYPE = "SunX509";
    private final Logger logger;
    private final AnsweringHandler handler;
    private final CertificatesManager certificatesManager;
    private final Environment environment;
    private boolean running =false;
;
    private HttpsServer httpsServer;



    public AnsweringHttpsServer(LoggerBuilder loggerBuilder, AnsweringHandler handler,
                                CertificatesManager certificatesManager, Environment environment,
                                JsonConfiguration configuration){
        this.logger = loggerBuilder.build(AnsweringHttpsServer.class);
        this.handler = handler;
        this.certificatesManager = certificatesManager;
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        var config = configuration.getConfiguration( HttpsWebServerConfig.class).copy();
        if(running)return;
        if(!config.isActive())return;
        running=true;

        try {

            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(config.getPort());

            // initialise the HTTPS server
            httpsServer = HttpsServer.create(address, config.getBacklog());
            final SSLContext sslContextInt = getSslContext();
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContextInt) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext context = sslContextInt;//getSslContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        //sslParameters.setProtocols(new String[]{"TLSv1.3","TLSv1.2","TLSv1.0"});
                        //(sslParameters.setCipherSuites(new String[] {"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"});
                        params.setSSLParameters(sslParameters);

                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });
            httpsServer.createContext("/", handler);
            if(config.isUseCachedExecutor()) {
                httpsServer.setExecutor(Executors.newCachedThreadPool());    // creates a cached
            }else {
                httpsServer.setExecutor(null);   // creates a default executor
            }
            httpsServer.start();
            logger.info("Https server LOADED, port: "+ config.getPort());
            var localConfig = configuration.getConfiguration( HttpsWebServerConfig.class);
            while(running && localConfig.isActive()){
                Thread.sleep(10000);
            }

        } catch (Exception ex) {
            logger.error("Failed to create HTTPS server on port " + config.getPort() + " of localhost",ex);
        }finally {
            running=false;
        }
    }

    private SSLContext getSslContext() throws Exception {
        var root = certificatesManager.loadRootCertificate("certificates/ca.der","certificates/ca.key");

        var sslConfig = configuration.getConfiguration(SSLConfig.class);
        GeneratedCert domain = certificatesManager.createCertificate(sslConfig.getCname(),null, root,
                sslConfig.getDomains().stream().map(sslDomain -> sslDomain.getAddress()).collect(Collectors.toList()), false);

        KeyStore ksTemp = KeyStore.getInstance("JKS");
        ksTemp.load(null, null); //Initialize it
        ksTemp.setCertificateEntry("Alias",domain.certificate);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        // save the temp keystore
        ksTemp.store(bOut, "passphrase".toCharArray());
        //Now create the keystore to be used by jsse
        KeyStore keyStoreTs = KeyStore.getInstance("JKS");
        keyStoreTs.load(new ByteArrayInputStream(bOut.toByteArray()), "passphrase".toCharArray());

        // now lets do the same with the keystore
        //noinspection DuplicatedCode
        KeyStore ksTemp2 = KeyStore.getInstance("JKS");
        ksTemp2.load(null, null); //Initialize it
        ksTemp2.setCertificateEntry("Alias", domain.certificate);
        ByteArrayOutputStream bOut2 = new ByteArrayOutputStream();
        // save the temp keystore
        ksTemp2.store(bOut2, "passphrase".toCharArray());
        //Now create the keystore to be used by jsse
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new ByteArrayInputStream(bOut2.toByteArray()), "passphrase".toCharArray());
        //HERE IS THE CHAIN
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = domain.certificate;
        keyStore.setKeyEntry("privateCert", domain.privateKey, "passphrase".toCharArray(), chain);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStoreTs);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "passphrase".toCharArray());

        // create SSLContext to establish the secure connection
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        logger.info("Https certificates generated");
        return ctx;
    }

    @Override
    public boolean shouldRun() {
        var localConfig = configuration.getConfiguration( HttpsWebServerConfig.class);
        return localConfig.isActive() && !running;
    }

    public void stop(){
        httpsServer.stop(1);
        running = false;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

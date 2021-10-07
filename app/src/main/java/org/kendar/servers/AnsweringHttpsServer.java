package org.kendar.servers;

/**
 * https://stackoverflow.com/questions/67720003/tls-1-3-server-socket-with-java-11-and-self-signed-certificates
 */

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.kendar.servers.certificates.CertificatesManager;
import org.kendar.servers.certificates.GeneratedCert;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AnsweringHttpsServer  implements AnsweringServer{
    public void isSystem(){};
    private static final String SSL_CONTEXT_TYPE = "TLS";
    private static final String KEYSTORE_INSTANCE_TYPE = "JKS";
    private static final String CERTIFICATE_TYPE = "SunX509";
    private final Logger logger;
    private final AnsweringHandler handler;
    private final CertificatesManager certificatesManager;
    private final Environment environment;
    private boolean running =false;

    @Value( "${https.port:443}" )
    private int port;
    @Value( "${https.backlog:50}" )
    private int backlog;
    @Value( "${https.enabled:true}" )
    private boolean enabled;
    @Value( "${https.useCachedExecutor:true}" )
    private boolean useCachedExecutor;
    //@Value( "${https.certificates.cnname}" )
    //private String cnName;
;
    private HttpsServer httpsServer;

    class CertificatesConfiguration{
        public String cname;
        public List<String> extraDomains = new ArrayList<>();
        public long timestamp = Calendar.getInstance().getTimeInMillis();
    }
    //private final ConcurrentLinkedQueue<String> extraDomains = new ConcurrentLinkedQueue<>();
    private final AtomicReference<CertificatesConfiguration> certificatesConfiguration;

    public List<String> getExtraDomains(){
        return certificatesConfiguration.get().extraDomains;
    }

    public void setExtraDomains(List<String> extraDomains){
        var oldConfig = certificatesConfiguration.get();
        var newConfig = new CertificatesConfiguration();
        newConfig.cname = oldConfig.cname;
        newConfig.extraDomains = extraDomains;
        certificatesConfiguration.set(newConfig);
    }

    public AnsweringHttpsServer(LoggerBuilder loggerBuilder, AnsweringHandler handler,
                                CertificatesManager certificatesManager, Environment environment){
        this.logger = loggerBuilder.build(AnsweringHttpsServer.class);
        this.handler = handler;
        this.certificatesManager = certificatesManager;
        this.environment = environment;
        var config = new CertificatesConfiguration();
        certificatesConfiguration = new AtomicReference<>(config);
    }

    @PostConstruct
    protected void postConstruct(){
        //extraDomains.add(localHostName);
        var config = new CertificatesConfiguration();
        certificatesConfiguration.set(config);
        for(int i=0;i<1000;i++){
            var index = "https.certificate."+Integer.toString(i);
            var certificateDomain = environment.getProperty(index);
            if(certificateDomain != null){
                config.extraDomains.add(certificateDomain);
            }
        }
    }

    @Override
    public void run() {
        if(running)return;
        if(!enabled)return;
        running=true;

        try {

            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(port);

            // initialise the HTTPS server
            httpsServer = HttpsServer.create(address, backlog);
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
            if(useCachedExecutor) {
                httpsServer.setExecutor(Executors.newCachedThreadPool());    // creates a cached
            }else {
                httpsServer.setExecutor(null);   // creates a default executor
            }
            httpsServer.start();
            logger.info("Https server LOADED, port: "+port);
            while(running){
                Thread.sleep(10000);
            }

        } catch (Exception ex) {
            logger.error("Failed to create HTTPS server on port " + port + " of localhost",ex);
        }finally {
            running=false;
        }
    }

    private SSLContext getSslContext() throws Exception {
        var root = certificatesManager.loadRootCertificate("certificates/ca.der","certificates/ca.key");
        var config = certificatesConfiguration.get();

        GeneratedCert domain = certificatesManager.createCertificate(config.cname,null, root,
                config.extraDomains,false);

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

    private List<String> toList(ConcurrentLinkedQueue<String> extraDomains) {
        var result = new ArrayList<String>();
        for(var item:extraDomains.toArray()){
            result.add((String)item);
        }
        return result;
    }

    @Override
    public boolean shouldRun() {
        return enabled && !running;
    }


    public void setPort(int port){
        this.port = port;
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

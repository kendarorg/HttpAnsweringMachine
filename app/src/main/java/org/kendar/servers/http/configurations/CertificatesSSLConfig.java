package org.kendar.servers.http.configurations;

import org.kendar.servers.http.configurations.CertificatesConfiguration;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class CertificatesSSLConfig {

    private final AtomicReference<CertificatesConfiguration> config = new AtomicReference<>();
    public void set(CertificatesConfiguration config){
        this.config.set(config);
    }

    public CertificatesConfiguration get(){
        return config.get();
    }
}

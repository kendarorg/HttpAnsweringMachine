package org.kendar.servers.http;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class FilterConfig {
    private final AtomicReference<FiltersConfiguration> config = new AtomicReference<>();
    public void set(FiltersConfiguration config){
        this.config.set(config);
    }

    public FiltersConfiguration get(){
        return config.get();
    }
}

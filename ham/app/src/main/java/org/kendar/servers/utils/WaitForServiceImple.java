package org.kendar.servers.utils;

import org.kendar.events.EventQueue;
import org.kendar.events.ServiceStarted;
import org.kendar.servers.WaitForService;
import org.kendar.utils.Sleeper;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WaitForServiceImple implements WaitForService {
    private ConcurrentHashMap<String, String> services = new ConcurrentHashMap<>();

    public WaitForServiceImple(EventQueue eventQueue) {
        eventQueue.register(this::handleServiceStarted, ServiceStarted.class);

    }

    private void handleServiceStarted(ServiceStarted t) {
        services.put(t.getType().toLowerCase(Locale.ROOT), t.getType());
    }

    public void waitForService(String name) {
        while (!services.containsKey(name.toLowerCase(Locale.ROOT))) {
            Sleeper.sleep(1000);
        }
    }

    @Override
    public void waitForService(String name, Runnable runnable) {
        if (services.containsKey(name.toLowerCase(Locale.ROOT))) {
            runnable.run();
            return;
        }
        var th = new Thread(() -> {
            while (!services.containsKey(name.toLowerCase(Locale.ROOT))) {
                Sleeper.sleep(1000);
            }
            runnable.run();
        });
        th.start();
    }
}

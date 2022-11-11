package org.kendar.servers;

import org.h2.tools.Server;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class AnsweringH2DbServer  implements AnsweringServer{

    private final Logger logger;
    private final AnsweringHandler handler;
    private final JsonConfiguration configuration;

    private boolean running = false;
    private Server server;

    public AnsweringH2DbServer(
            LoggerBuilder loggerBuilder, AnsweringHandler handler, JsonConfiguration configuration) {
        this.logger = loggerBuilder.build(AnsweringH2DbServer.class);
        this.handler = handler;
        this.configuration = configuration;
    }

    public void isSystem() {
        //To check if is a system class
    }

    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(GlobalConfig.class)
                .getDb().copy();
        if (!config.isStartInternalH2()) return;
        running = true;
        try{
            final String userDir = System.getProperty("user.dir");
            server = Server.createTcpServer("-baseDir", userDir + "/data","-tcpAllowOthers","-ifNotExists");
            server.start();


            Connection conn = null;
            try {
                Class.forName("org.h2.Driver");
                conn = DriverManager.getConnection(config.getUrl(), config.getLogin(), config.getPassword());
            } finally {
                if (conn != null)
                    conn.close();
            }

            while(running){
                Sleeper.sleep(60*1000);
            }
        } catch (Exception ex) {
            logger.error(
                    "Failed to create H2 local server on " + config.getUrl(), ex);
        } finally {
            running = false;
        }
    }

    @Override
    public boolean shouldRun() {
        var config = configuration.getConfiguration(GlobalConfig.class)
                .getDb().copy();
        return config.isStartInternalH2() && !running;
    }
}

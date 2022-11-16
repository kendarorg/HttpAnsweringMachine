package org.kendar.servers;

import org.h2.tools.Server;
import org.hibernate.boot.MetadataSources;
import org.hibernate.cfg.Configuration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.config.GlobalConfigDb;
import org.kendar.servers.db.DbTable;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.servers.logging.LoggingDataTable;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

@Component
public class AnsweringH2DbServer  implements AnsweringServer{

    private final Logger logger;
    private final AnsweringHandler handler;
    private final JsonConfiguration configuration;
    private List<DbTable> dbTableList;
    private HibernateSessionFactory sessionFactory;

    private boolean running = false;
    private boolean initialized = false;
    private Server server;

    public AnsweringH2DbServer(
            LoggerBuilder loggerBuilder, AnsweringHandler handler, JsonConfiguration configuration,
            List<DbTable> dbTableList, HibernateSessionFactory sessionFactory) {
        this.logger = loggerBuilder.build(AnsweringH2DbServer.class);
        this.handler = handler;
        this.configuration = configuration;
        this.dbTableList = dbTableList;
        this.sessionFactory = sessionFactory;
    }

    public void isSystem() {
        //To check if is a system class
    }

    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(GlobalConfig.class)
                .getDb().copy();
        try{

            if (!config.isStartInternalH2() && !initialized) {
                initializeDb(config);
                return;
            }
            running = true;

            final String userDir = System.getProperty("user.dir");
            server = Server.createTcpServer("-baseDir", userDir + "/data","-tcpAllowOthers","-ifNotExists");
            server.start();

            if(!initialized){
                initializeDb(config);
            }
            /**/
            logger.info("H2 DB server LOADED, port: {}",server.getPort());

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

    private void initializeDb(GlobalConfigDb config) throws ClassNotFoundException {
        try {
            initialized = true;
            Class.forName(config.getDriver());
            var hibernateConfig = new Configuration();

            /*
            Properties properties = new Properties();
properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
properties.put("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
properties.put("hibernate.connection.url", "jdbc:mysql://localhost:3306/kode12");
properties.put("hibernate.connection.username", "root");
properties.put("hibernate.connection.password", "root");
properties.put("show_sql", "true");
properties.put("hbm2ddl.auto", "update");
configuration.setProperties(properties);
             */

            hibernateConfig.setProperty("hibernate.connection.driver_class", config.getDriver());
            hibernateConfig.setProperty("hibernate.connection.url", config.getUrl());
            hibernateConfig.setProperty("hibernate.connection.username", config.getLogin());
            hibernateConfig.setProperty("hibernate.connection.password", config.getPassword());
            hibernateConfig.setProperty("hibernate.dialect", config.getHibernateDialect());
            hibernateConfig.setProperty("show_sql", "true");
            hibernateConfig.setProperty("hibernate.hbm2ddl.auto", "update");
            for (var table : dbTableList) {
                hibernateConfig.addAnnotatedClass(table.getClass());
            }
            this.sessionFactory.setConfiguration(hibernateConfig);

        }catch (Exception ex){
            logger.error("Error building tables"+ex);
        }


    }

    @Override
    public boolean shouldRun() {
        var config = configuration.getConfiguration(GlobalConfig.class)
                .getDb().copy();
        return config.isStartInternalH2() && !running;
    }
}

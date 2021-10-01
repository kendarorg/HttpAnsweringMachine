package org.kendar.servers;

import org.apache.derby.drda.NetworkServerControl;
import org.kendar.servers.db.DerbyApplication;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class AnsweringDerbyServer implements AnsweringServer{
    public void isSystem(){};
    private final Logger logger;
    private final List<DerbyApplication> applicationList;
    private final Environment environment;
    private final FileResourcesUtils fileResourcesUtils;

    public AnsweringDerbyServer(LoggerBuilder loggerBuilder,
                                List<DerbyApplication> applicationList, Environment environment,
                                FileResourcesUtils fileResourcesUtils){
        logger = loggerBuilder.build(AnsweringDerbyServer.class);
        this.applicationList = applicationList;
        this.environment = environment;
        this.fileResourcesUtils = fileResourcesUtils;
    }
    @Value("${derby.port:1527}")
    private int port;
    private boolean running =false;
    @Value( "${derby.enabled:false}" )
    private final boolean enabled =true;
    @Value("${derby.root.user:root")
    private String rootUser;
    @Value("${derby.root.password:root")
    private String rootPassword;
    @Value("${derby.path:derbydata}")
    private String dbPath;
    @Value("${derby.url:null}")
    private String derbyBaseUrl;
    @Value("${derby.driver:null}")
    private String derbyDriver;
    @Override
    public void run() {
        if(running)return;
        if(!enabled)return;
        running=true;
        NetworkServerControl nsc;
        try {
            //https://www.vogella.com/tutorials/ApacheDerby/article.html
            Class.forName(derbyDriver);
            var realDbPath = fileResourcesUtils.buildPath(dbPath);
            System.setProperty("derby.system.home", realDbPath);
            nsc = new NetworkServerControl(InetAddress.getByName("0.0.0.0"), port,rootUser,rootPassword);
            nsc.start(null);
            waitForDerbyStart(nsc);
            initializeApplications();

            logger.info("Derby server LOADED, port: "+port);
            while(true){
                try {
                    Thread.sleep(1000);
                    nsc.ping();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        running = false;
    }

    private void initializeApplications() {
        for (var application : applicationList) {
            ResultSet rs =null;
            Connection conn =null;
            try {
                //conn = DriverManager.getConnection(derbyBaseUrl+"/"+application.dbName()+";create=true");
                conn = DriverManager.getConnection(application.connectionString());
                var dmd = conn.getMetaData();
                rs = dmd.getTables(null, "APP", application.canaryTable(), null);

                var firstTime = !rs.next();
                rs.close();
                if(firstTime){
                    application.initializeDb(conn);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }finally {
                try {
                    if (rs != null) rs.close();
                    if (conn != null) conn.close();
                }catch(Exception ex){

                }
            }
        }
    }

    private void waitForDerbyStart(NetworkServerControl nsc) throws InterruptedException {
        for (int i = 0; i < 10; ++i) {
            try {
                nsc.ping();
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            Thread.sleep(10);
        }
    }



    @Override
    public boolean shouldRun() {
        return enabled && !running;
    }
}

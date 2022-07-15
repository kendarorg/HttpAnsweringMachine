package org.kendar.servers;

import org.apache.derby.drda.NetworkServerControl;
import org.kendar.servers.db.DerbyApplication;
import org.kendar.servers.db.DerbyServerConfig;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class AnsweringDerbyServer implements AnsweringServer {
  private final Logger logger;
  private final List<DerbyApplication> applicationList;
  private final FileResourcesUtils fileResourcesUtils;
  private final JsonConfiguration configuration;
  private boolean running = false;

  public AnsweringDerbyServer(
      LoggerBuilder loggerBuilder,
      List<DerbyApplication> applicationList,
      FileResourcesUtils fileResourcesUtils,
      JsonConfiguration configuration) {
    logger = loggerBuilder.build(AnsweringDerbyServer.class);
    this.applicationList = applicationList;
    this.fileResourcesUtils = fileResourcesUtils;
    this.configuration = configuration;
  }

  public void isSystem() {
    //To define system processes
  }

  @Override
  public void run() {
    var config = configuration.getConfiguration(DerbyServerConfig.class);
    if (running) return;
    if (!config.isActive()) return;
    running = true;
    NetworkServerControl nsc;
    try {
      // https://www.vogella.com/tutorials/ApacheDerby/article.html
      Class.forName(config.getDerbyDriver());
      var realDbPath = fileResourcesUtils.buildPath(config.getPath());
      System.setProperty("derby.system.home", realDbPath);
      nsc = startNetworkServerControl(config);
      waitForDerbyStart(nsc);
      initializeApplications();

      logger.info("Derby server LOADED, port: {}",config.getPort());
      while (config.isActive()) {
        config = getDerbyConfigWhenActive(config, nsc);
      }
    } catch (ClassNotFoundException e) {
      logger.error("Error starting derby",e);
    }
    running = false;
  }

  private NetworkServerControl startNetworkServerControl(DerbyServerConfig config) {
    NetworkServerControl nsc;
    try {
      nsc =
          new NetworkServerControl(
              InetAddress.getByName("0.0.0.0"),
              config.getPort(),
              config.getUser(),
              config.getPassword());
      nsc.start(null);
    }catch (Exception ex){
      throw new IllegalStateException("Unable to start derby");
    }
    return nsc;
  }

  private DerbyServerConfig getDerbyConfigWhenActive(DerbyServerConfig config, NetworkServerControl nsc) {
    try {
      Sleeper.sleep(1000);
      nsc.ping();
      config = configuration.getConfiguration(DerbyServerConfig.class);
    } catch (Exception e) {
      logger.warn("Error loading config",e);
    }
    return config;
  }

  private void initializeApplications() {
    for (var application : applicationList) {
      ResultSet rs = null;
      Connection conn = null;
      try {
        // Connection string: derbyBaseUrl/application.dbName();create=true
        conn = DriverManager.getConnection(application.connectionString());
        var dmd = conn.getMetaData();
        rs = dmd.getTables(null, "APP", application.canaryTable(), null);

        var firstTime = !rs.next();
        rs.close();
        if (firstTime) {
          application.initializeDb(conn);
        }
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      } finally {
        try {
          if (rs != null) rs.close();
          if (conn != null) conn.close();
        } catch (Exception ex) {
          logger.trace(ex.getMessage());
        }
      }
    }
  }

  private void waitForDerbyStart(NetworkServerControl nsc)  {
    for (int i = 0; i < 10; ++i) {
      try {
        nsc.ping();
        break;
      } catch (Exception e) {
        logger.warn(e.getMessage());
      }
      Sleeper.sleep(10);
    }
  }

  @Override
  public boolean shouldRun() {
    var localConfig = configuration.getConfiguration(DerbyServerConfig.class);
    return localConfig.isActive() && !running;
  }
}

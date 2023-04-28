package org.kendar.cucumber;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.cucumber.java.en.Given;
import org.kendar.globaltest.LocalFileUtils;
import org.kendar.globaltest.ProcessRunner;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

import static org.kendar.cucumber.Utils.*;
import static org.kendar.globaltest.LocalFileUtils.pathOf;

public class DbTasks {
    public static int MONGO_PORT = 27077;
    private MongoServer mongoServer;

    @Given("^Start h2 db$")
    public void startH2Db() throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");
        var root = getRootPath(DbTasks.class);
        Map<String, String> env = new HashMap<>();
        new ProcessRunner(env).
                asShell().
                withCommand("rundb" + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar")).
                runBackground();
        showMessage("Started H2 Database");
        Sleeper.sleep(1000);
    }

    @Given("^Prepare mongo proxy$")
    public void prepareMongoProxy() throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");
        driver.get("http://www.local.test/index.html");
        Sleeper.sleep(1000);
        takeSnapShot();

        doClick(() -> driver.findElement(By.linkText("Mongo proxy")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mongoprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringR")));
        Sleeper.sleep(1000);
        sendKeys(By.id("connectionStringR"),"mongodb://127.0.0.1:27077");
        Sleeper.sleep(1000);
        checkCheckBox(() -> driver.findElement(By.id("active")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("exposedPort")));
        Sleeper.sleep(1000);
        sendKeys(By.id("exposedPort"),"27078");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
    }

    @Given("^Start mongodb$")
    public void startMongodb() throws Exception {
        mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("127.0.0.1", MONGO_PORT);
        org.kendar.utils.Sleeper.sleep(500);
    }

    @Given("^Stop mongodb$")
    public void stopMongodb() throws Exception {
        if (mongoServer == null) return;
        mongoServer.shutdown();
        org.kendar.utils.Sleeper.sleep(500);
    }
}

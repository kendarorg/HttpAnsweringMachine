package org.kendar;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.ProcessUtils;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashMap;
import java.util.Map;

import static org.kendar.DbRecordingSetupTest.stopAction;
import static org.kendar.SeleniumBase.*;

public class MongoRecordingSetupTest {
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());



    public static String startRecording(ChromeDriver driver, String idRecording) throws Exception {
        System.out.println("******* MongoRecordingSetupTest::startRecording");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Replayer web")));
        Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.id("main-recording-addnew")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("createScriptName")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("createScriptName")).sendKeys(idRecording);
        Sleeper.sleep(1000);
        scrollFind(driver,() -> driver.findElement(By.id("createScriptBt"))).click();
        Sleeper.sleep(2000);

        driver.get("http://www.local.test/plugins/recording/script.html?id=1");

        try {
            Sleeper.sleep(1000);
            scrollFind(driver, () -> driver.findElement(By.id("scriptstab_0"))).click();
        }catch (Exception ex){

        }
        Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.id("description")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("description")).sendKeys("Full recording sample");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Sleeper.sleep(1000);
        showMessage(driver,"Starting recording");
        doClick(() -> driver.findElement(By.id("recording-startrecord")));
        Sleeper.sleep(1000);
        showMessage(driver,"Starting be without db initialisation");
        //start the "nogen" and wait for its start
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
        run(root, env, "bemongo");
        HttpChecker.checkForSite(120, "http://127.0.0.1:8100/api/v1/health")
                .noError().run();
        var result = driver.findElement(By.id("id")).getAttribute("value");
        return result;
    }

    public static int MONGO_PORT = 27077;

    public static void initializeNullPlayingDb(ChromeDriver driver, String dbNullTest) throws Exception {
        System.out.println("******* MongoRecordingSetupTest::initializeNullPlayingDb");
        //Setup the application
        doClick(() -> driver.findElement(By.id("recording-play")));
        Sleeper.sleep(1000);
        var root = getRootPath(DbRecordingSetupTest.class);
        showMessage(driver,"Starting mongobe");
        Map<String, String> env = new HashMap<>();
        //run(root, env, "bemongo");
        HttpChecker.checkForSite(120, "http://127.0.0.1:8100/api/v1/health")
                .noError().run();
        stopAction(driver, dbNullTest);
    }



    public static void startup(ChromeDriver driver) throws Exception {
        System.out.println("******* MongoRecordingSetupTest::startup");
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
       startMongo();

        var js = (JavascriptExecutor)driver;
        Sleeper.sleep(1000);


        run(root, env, "gateway");
        run(root, env, "fe");


        Sleeper.sleep(1000);
        driver.get("http://www.local.test/index.html");
        setupSize(driver);
        Sleeper.sleep(2000);
        showMessage(driver,"Started gateway, fe and fake mongo db");
        doClick(() -> driver.findElement(By.linkText("Url/Db Rewrites")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/gateway.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8090");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8090");
        Sleeper.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("force")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/be.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8100");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8100");
        Sleeper.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("force")));
        doClick(() -> driver.findElement(By.id("mod-save")));
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        driver.findElement(By.id("when")).sendKeys("http://www.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8080");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8080");
        Sleeper.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("force")));
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);


        driver.get("http://www.local.test/index.html");
        Sleeper.sleep(1000);

        doClick(() ->driver.findElement(By.linkText("Mongo proxy")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mongoprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringR")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("connectionStringR")).sendKeys("mongodb://127.0.0.1:27077");
        Sleeper.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("active")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("exposedPort")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("exposedPort")).sendKeys("27078");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.linkText("Main")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Dns")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("MAPPINGS")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns-mappings-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("dns")).sendKeys("www.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns-mappings-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns")));
        driver.findElement(By.id("dns")).sendKeys("gateway.sample.test");
        doClick(() -> driver.findElement(By.id("mod-save")));
        doClick(() -> driver.findElement(By.id("dns-mappings-add")));
        doClick(() -> driver.findElement(By.id("dns")));
        driver.findElement(By.id("dns")).sendKeys("be.sample.test");
        doClick(() -> driver.findElement(By.id("mod-save")));
        doClick(() -> driver.findElement(By.linkText("Main")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("SSL/Certificates")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("ssl-sites-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("address")));
        driver.findElement(By.id("address")).sendKeys("*.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("ssl-sites-add")));
        doClick(() -> driver.findElement(By.id("address")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("address")).sendKeys("sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.linkText("Main")));

        HttpChecker.checkForSite(120, "http://127.0.0.1:8100/api/v1/health")
                .noError().run();

        showMessage(driver,"Stopping be-MongoDb");
        //Kill the be that initialized the system
        var version = SeleniumBase.getVersion();
        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                                (psLine.contains("bemongo-" + version))) &&
                        !psLine.contains("globaltest"));
    }

    public static MongoServer mongoServer;
    public static void stopMongo() {
        mongoServer.shutdown();
        mongoServer=null;
        org.kendar.utils.Sleeper.sleep(500);
    }

    public static void startMongo() {
        mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("127.0.0.1",MONGO_PORT);
        org.kendar.utils.Sleeper.sleep(500);
    }
}

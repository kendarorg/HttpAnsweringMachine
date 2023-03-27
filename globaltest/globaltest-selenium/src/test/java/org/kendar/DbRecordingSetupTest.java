package org.kendar;

import org.apache.commons.lang3.NotImplementedException;
import org.kendar.globaltest.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;


import org.kendar.ham.HamReplayerBuilder;
import org.kendar.ham.HamBuilder;
import org.kendar.ham.HamException;


import java.util.HashMap;
import java.util.Map;

import static org.kendar.SeleniumBase.*;
import static org.kendar.globaltest.LocalFileUtils.pathOf;

public class DbRecordingSetupTest {
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());


    public static void startup(ChromeDriver driver) throws Exception {
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
        new ProcessRunner(env).
                asShell().
                withCommand("rundb" + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar")).
                runBackground();

        var js = (JavascriptExecutor)driver;
        Sleeper.sleep(1000);


        run(root, env, "gateway");
        run(root, env, "fe");


        Sleeper.sleep(1000);
        driver.get("http://www.local.test/index.html");
        setupSize(driver);
        Sleeper.sleep(2000);
        showMessage(driver,"Started gateway, fe and h2 db");
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
        doClick(() -> driver.findElement(By.id("force")));
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
        doClick(() -> driver.findElement(By.linkText("JDBC PROXIES")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("jdbcprx-grid-addnew")));
        doClick(() -> driver.findElement(By.id("driver")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("driver")).sendKeys("org.h2.Driver");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringR")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("connectionStringR")).sendKeys("jdbc:h2:tcp://localhost:9123/./data/be;MODE=MYSQL;");
        doClick(() -> driver.findElement(By.id("loginR")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("loginR")).sendKeys("sa");
        Sleeper.sleep(1000);
        driver.findElement(By.cssSelector("span > div")).click();
        doClick(() -> driver.findElement(By.id("passwordR")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("passwordR")).sendKeys("sa");
        Sleeper.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("active")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringL")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("connectionStringL")).sendKeys("be");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("loginL")));
        driver.findElement(By.id("loginL")).sendKeys("login");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("passwordL")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("passwordL")).sendKeys("password");
        Sleeper.sleep(1000);
        scrollFind(driver,() -> driver.findElement(By.id("mod-save"))).click();
        Sleeper.sleep(1000);
        run(root, env, "bedbham");

        showMessage(driver,"Started be");

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

        showMessage(driver,"Stopping be");
        //Kill the be that initialized the system
        var version = SeleniumBase.getVersion();
        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                                (psLine.contains("be-" + version))) &&
                                !psLine.contains("globaltest"));

    }

    public static String startRecording(ChromeDriver driver, String idRecording) throws Exception {
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Replayer web")));
        Sleeper.sleep(1000);
        scrollFind(driver,()->driver.findElement(By.id("scriptstab_0"))).click();
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

        scrollFind(driver,() -> driver.findElement(By.id("extdbname"))).click();
        driver.findElement(By.id("extdbname")).clear();
        Sleeper.sleep(1000);
        driver.findElement(By.id("extdbname")).sendKeys("be");
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
        run(root, env, "benogen");
        HttpChecker.checkForSite(120, "http://127.0.0.1:8100/api/v1/health")
                .noError().run();
        var result = driver.findElement(By.id("id")).getAttribute("value");
        return result;
    }


    public static void stopAction(ChromeDriver driver, String idRecording) throws InterruptedException {
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + idRecording);
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-stop")));
        Sleeper.sleep(3000);
        doClick(() -> driver.findElement(By.id("recording-list-reload")));
        Sleeper.sleep(1000);
    }





    public static void startPlaying(ChromeDriver driver, String idRecording) throws InterruptedException {
        showMessage(driver,"Starting replay");
        doClick(() -> driver.findElement(By.id("recording-play")));
        Sleeper.sleep(1000);
    }

    public static void startNullPlaying(ChromeDriver driver, String idRecording) throws Exception {
        showMessage(driver,"Starting replay with self-test");
        scrollFind(driver, () -> driver.findElement(By.id("recording-playstim"))).click();
        Sleeper.sleep(2000);
    }

    public static void loadResults(ChromeDriver driver, String idRecording) throws Exception {
        doClick(() -> driver.findElement(By.linkText("RESULTS")));
        Sleeper.sleep(5000);
        scrollFind(driver,() -> driver.findElement(By.id("recording-grid-result-reload")),100).click();
        Sleeper.sleep(1000);
        var builder = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");
        var replayer = builder.pluginBuilder(HamReplayerBuilder.class);
        var results = replayer.retrieveResults(Integer.parseInt(idRecording));
        var result = results.get(0);
        var resultIndex = result.getFileId();
        Sleeper.sleep(1000);
        driver.get("http://www.local.test/api/plugins/replayer/results/" + resultIndex);
        Sleeper.sleep(2000);
    }

    public static void initializeNullPlayingDb(ChromeDriver driver, String dbNullTest) throws Exception {
        //Setup the application
        doClick(() -> driver.findElement(By.id("recording-play")));
        Sleeper.sleep(1000);
        var root = getRootPath(DbRecordingSetupTest.class);
        showMessage(driver,"Starting be without initializing db");
        Map<String, String> env = new HashMap<>();
        run(root, env, "benogen");
        HttpChecker.checkForSite(120, "http://127.0.0.1:8100/api/v1/health")
                .noError().run();
        stopAction(driver, dbNullTest);
    }
}

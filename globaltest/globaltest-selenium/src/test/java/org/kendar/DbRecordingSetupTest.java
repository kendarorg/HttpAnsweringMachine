package org.kendar;

import org.apache.commons.lang3.NotImplementedException;
import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.LocalFileUtils;
import org.kendar.globaltest.ProcessRunner;
import org.kendar.globaltest.ProcessUtils;
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
        Thread.sleep(1000);


        run(root, env, "gateway");
        run(root, env, "fe");


        Thread.sleep(1000);
        driver.get("http://www.local.test/index.html");
        setupSize(driver);
        Thread.sleep(2000);
        showMessage(driver,"Started gateway, fe and h2 db");
        doClick(() -> driver.findElement(By.linkText("Url/Db Rewrites")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        Thread.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/gateway.sample.test");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Thread.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8090");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        Thread.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8090");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("force")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        Thread.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/be.sample.test");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Thread.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8100");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        Thread.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8100");
        Thread.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("force")));
        doClick(() -> driver.findElement(By.id("mod-save")));
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        driver.findElement(By.id("when")).sendKeys("http://www.sample.test");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Thread.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8080");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8080");
        Thread.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("force")));
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("JDBC PROXIES")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("jdbcprx-grid-addnew")));
        doClick(() -> driver.findElement(By.id("driver")));
        Thread.sleep(1000);
        driver.findElement(By.id("driver")).sendKeys("org.h2.Driver");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringR")));
        Thread.sleep(1000);
        driver.findElement(By.id("connectionStringR")).sendKeys("jdbc:h2:tcp://localhost:9123/./data/be;MODE=MYSQL;");
        doClick(() -> driver.findElement(By.id("loginR")));
        Thread.sleep(1000);
        driver.findElement(By.id("loginR")).sendKeys("sa");
        Thread.sleep(1000);
        driver.findElement(By.cssSelector("span > div")).click();
        doClick(() -> driver.findElement(By.id("passwordR")));
        Thread.sleep(1000);
        driver.findElement(By.id("passwordR")).sendKeys("sa");
        Thread.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.id("active")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringL")));
        Thread.sleep(1000);
        driver.findElement(By.id("connectionStringL")).sendKeys("be");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("loginL")));
        driver.findElement(By.id("loginL")).sendKeys("login");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("passwordL")));
        Thread.sleep(1000);
        driver.findElement(By.id("passwordL")).sendKeys("password");
        Thread.sleep(1000);
        scrollFind(driver,() -> driver.findElement(By.id("mod-save"))).click();
        Thread.sleep(1000);
        run(root, env, "bedbham");

        showMessage(driver,"Started be");

        doClick(() -> driver.findElement(By.linkText("Main")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Dns")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("MAPPINGS")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns-mappings-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns")));
        Thread.sleep(1000);
        driver.findElement(By.id("dns")).sendKeys("www.sample.test");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns-mappings-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns")));
        driver.findElement(By.id("dns")).sendKeys("gateway.sample.test");
        doClick(() -> driver.findElement(By.id("mod-save")));
        doClick(() -> driver.findElement(By.id("dns-mappings-add")));
        doClick(() -> driver.findElement(By.id("dns")));
        driver.findElement(By.id("dns")).sendKeys("be.sample.test");
        doClick(() -> driver.findElement(By.id("mod-save")));
        doClick(() -> driver.findElement(By.linkText("Main")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("SSL/Certificates")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("ssl-sites-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("address")));
        driver.findElement(By.id("address")).sendKeys("*.sample.test");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("ssl-sites-add")));
        doClick(() -> driver.findElement(By.id("address")));
        Thread.sleep(1000);
        driver.findElement(By.id("address")).sendKeys("sample.test");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);

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
        driver.get("http://www.local.test/index.html");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Replayer web")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("main-recording-addnew")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("createScriptName")));
        Thread.sleep(1000);
        driver.findElement(By.id("createScriptName")).sendKeys(idRecording);
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("createScriptBt")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.cssSelector(".col-md-8:nth-child(3) #name")));
        driver.findElement(By.cssSelector(".col-md-8:nth-child(3) #name")).clear();
        Thread.sleep(1000);
        driver.findElement(By.cssSelector(".col-md-8:nth-child(3) #name")).sendKeys("be");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("description")));
        Thread.sleep(1000);
        driver.findElement(By.id("description")).sendKeys("Full recording sample");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Thread.sleep(1000);
        showMessage(driver,"Starting recording");
        doClick(() -> driver.findElement(By.id("recording-startrecord")));
        Thread.sleep(1000);
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
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-stop")));
        Thread.sleep(3000);
        doClick(() -> driver.findElement(By.id("recording-list-reload")));
        Thread.sleep(1000);
    }





    public static void startPlaying(ChromeDriver driver, String idRecording) throws InterruptedException {
        showMessage(driver,"Starting replay");
        doClick(() -> driver.findElement(By.id("recording-play")));
        Thread.sleep(1000);
    }

    public static void startNullPlaying(ChromeDriver driver, String idRecording) throws Exception {
        showMessage(driver,"Starting replay with self-test");
        scrollFind(driver, () -> driver.findElement(By.id("recording-playstim"))).click();
        Thread.sleep(2000);
    }

    public static void loadResults(ChromeDriver driver, String idRecording) throws Exception {
        doClick(() -> driver.findElement(By.linkText("RESULTS")));
        Thread.sleep(1000);
        scrollFind(driver,() -> driver.findElement(By.id("recording-grid-result-reload")),100).click();
        Thread.sleep(1000);
        var builder = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");
        var replayer = builder.pluginBuilder(HamReplayerBuilder.class);
        var results = replayer.retrieveResults(Integer.parseInt(idRecording));
        var result = results.get(0);
        var resultIndex = result.getFileId();
        Thread.sleep(1000);
        driver.get("http://www.local.test/api/plugins/replayer/results/" + resultIndex);
        Thread.sleep(2000);
    }

    public static void initializeNullPlayingDb(ChromeDriver driver, String dbNullTest) throws Exception {
        //Setup the application
        doClick(() -> driver.findElement(By.id("recording-play")));
        Thread.sleep(1000);
        var root = getRootPath(DbRecordingSetupTest.class);
        showMessage(driver,"Starting be without initializing db");
        Map<String, String> env = new HashMap<>();
        run(root, env, "benogen");
        HttpChecker.checkForSite(120, "http://127.0.0.1:8100/api/v1/health")
                .noError().run();
        stopAction(driver, dbNullTest);
    }
}

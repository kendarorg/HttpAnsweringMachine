package org.kendar;

import org.apache.commons.lang3.NotImplementedException;
import org.kendar.globaltest.LocalFileUtils;
import org.kendar.globaltest.ProcessRunner;
import org.kendar.globaltest.ProcessUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;
import java.util.Map;

import static org.kendar.SeleniumBase.doClick;
import static org.kendar.SeleniumBase.getRootPath;
import static org.kendar.globaltest.LocalFileUtils.pathOf;

public class DbRecordingSetupTest {
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());



    private static ProcessRunner run(String root, Map<String, String> env, String script) throws Exception {
        return new ProcessRunner(env).
                asShell().
                withCommand(script + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar", "scripts")).
                runBackground();
    }
    public static void startup(FirefoxDriver driver) throws Exception {
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
        new ProcessRunner(env).
                asShell().
                withCommand("rundb" + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar")).
                runBackground();

        Thread.sleep(1000);


        run(root, env, "gateway");
        run(root, env, "fe");


        Thread.sleep(1000);
        driver.get("http://www.local.test/index.html");
        driver.manage().window().setSize(new Dimension(1024, 1024));

        Thread.sleep(2000);
        doClick(()->driver.findElement(By.linkText("Url/Db Rewrites")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("webprx-gird-add")));
        Thread.sleep(1000);
        driver.findElement(By.id("when")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/gateway.sample.test");
        Thread.sleep(1000);
        driver.findElement(By.id("where")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8090");
        Thread.sleep(1000);
        driver.findElement(By.id("test")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8090");
        Thread.sleep(1000);
        driver.findElement(By.id("force")).click();
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        driver.findElement(By.id("webprx-gird-add")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("when")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/be.sample.test");
        Thread.sleep(1000);
        driver.findElement(By.id("where")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8100");
        Thread.sleep(1000);
        driver.findElement(By.id("test")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8100");
        Thread.sleep(1000);
        driver.findElement(By.id("force")).click();
        doClick(()->driver.findElement(By.id("mod-save")));
        driver.findElement(By.id("webprx-gird-add")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("when")).click();
        driver.findElement(By.id("when")).sendKeys("http://www.sample.test");
        Thread.sleep(1000);
        driver.findElement(By.id("where")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8080");
        Thread.sleep(1000);
        driver.findElement(By.id("test")).click();
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8080");
        Thread.sleep(1000);
        driver.findElement(By.id("force")).click();
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.linkText("JDBC PROXIES")));
        Thread.sleep(1000);
        driver.findElement(By.id("jdbcprx-grid-addnew")).click();
        driver.findElement(By.id("driver")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("driver")).sendKeys("org.h2.Driver");
        Thread.sleep(1000);
        driver.findElement(By.id("connectionStringR")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("connectionStringR")).sendKeys("jdbc:h2:tcp://localhost:9123/./data/be;MODE=MYSQL;");
        driver.findElement(By.id("loginR")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("loginR")).sendKeys("sa");
        Thread.sleep(1000);
        driver.findElement(By.cssSelector("span > div")).click();
        driver.findElement(By.id("passwordR")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("passwordR")).sendKeys("sa");
        Thread.sleep(1000);
        driver.findElement(By.id("active")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("connectionStringL")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("connectionStringL")).sendKeys("be");
        Thread.sleep(1000);
        driver.findElement(By.id("loginL")).click();
        driver.findElement(By.id("loginL")).sendKeys("login");
        Thread.sleep(1000);
        driver.findElement(By.id("passwordL")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("passwordL")).sendKeys("password");
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        run(root, env, "bedbham");

        doClick(()->driver.findElement(By.linkText("Main")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.linkText("Dns")));
        Thread.sleep(1000);
        driver.findElement(By.linkText("MAPPINGS")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("dns-mappings-add")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("dns")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("dns")).sendKeys("www.sample.test");
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("dns-mappings-add")));
        Thread.sleep(1000);
        driver.findElement(By.id("dns")).click();
        driver.findElement(By.id("dns")).sendKeys("gateway.sample.test");
        doClick(()->driver.findElement(By.id("mod-save")));
        driver.findElement(By.id("dns-mappings-add")).click();
        driver.findElement(By.id("dns")).click();
        driver.findElement(By.id("dns")).sendKeys("be.sample.test");
        doClick(()->driver.findElement(By.id("mod-save")));
        doClick(()->driver.findElement(By.linkText("Main")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.linkText("SSL/Certificates")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("ssl-sites-add")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("address")));
        driver.findElement(By.id("address")).sendKeys("*.sample.test");
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        /*{
            WebElement element = driver.findElement(By.id("mod-save"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        Thread.sleep(1000);*/
        doClick(()->driver.findElement(By.id("ssl-sites-add")));
       /* {
            WebElement element = driver.findElement(By.id("ssl-sites-add"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }*/
        doClick(()->driver.findElement(By.id("address")));
        Thread.sleep(1000);
        driver.findElement(By.id("address")).sendKeys("sample.test");
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);

        doClick(()->driver.findElement(By.linkText("Main")));

    }

    public static String startRecording(FirefoxDriver driver, String idRecording) throws InterruptedException {
        driver.get("http://www.local.test/index.html");
        Thread.sleep(1000);
        driver.findElement(By.linkText("Replayer web")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("main-recording-addnew")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("createScriptName")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("createScriptName")).sendKeys(idRecording);
        Thread.sleep(1000);
        driver.findElement(By.id("createScriptBt")).click();
        Thread.sleep(1000);
        driver.findElement(By.cssSelector(".col-md-8:nth-child(3) #name")).click();
        driver.findElement(By.cssSelector(".col-md-8:nth-child(3) #name")).clear();
        Thread.sleep(1000);
        driver.findElement(By.cssSelector(".col-md-8:nth-child(3) #name")).sendKeys("be");
        Thread.sleep(1000);
        driver.findElement(By.id("description")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("description")).sendKeys("Full recording sample");
        Thread.sleep(1000);
        driver.findElement(By.id("recording-saverglobscriptdata")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("recording-startrecord")).click();
        Thread.sleep(1000);
        var result = driver.findElement(By.id("id")).getAttribute("value");
        return result;
    }



    public static void stopAction(FirefoxDriver driver, String idRecording) throws InterruptedException {
        driver.get("http://www.local.test/plugins/recording/script.html?id="+idRecording);
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("recording-stop")));
        Thread.sleep(3000);
        doClick(()->driver.findElement(By.id("recording-list-reload")));
        Thread.sleep(1000);
    }


    public static void analyzeRecording(FirefoxDriver driver, String idRecording) {
        throw new NotImplementedException();
    }

    public static String cloneTo(FirefoxDriver driver, String toCloneFrom, String recordingId) {
        throw new NotImplementedException();
    }

    public static String prepareFakeDbTest(FirefoxDriver driver, String recordingData,String recordingId) {
        throw new NotImplementedException();
    }

    public static void startPlaying(FirefoxDriver driver,String idRecording) {
        throw new NotImplementedException();
    }

    public static void startNullPlaying(FirefoxDriver driver,String idRecording) {
        throw new NotImplementedException();
    }
    public static void loadResults(FirefoxDriver driver,String idRecording) {
        throw new NotImplementedException();
    }

    public static void prepareUiTest(FirefoxDriver driver, String uiTestId) {
        throw new NotImplementedException();
    }

    public static void prepareGatewayNullTest(FirefoxDriver driver, String uiTestId) {
        throw new NotImplementedException();
    }

    public static void prepareDbNullTest(FirefoxDriver driver, String dbNullTest) {
        throw new NotImplementedException();
    }
}

package org.kendar;

import org.kendar.globaltest.ProcessUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;
import java.util.Map;

import static org.kendar.SeleniumBase.*;

public class DbRecordingPrepareTest {
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());

    public static void prepareUiTest(FirefoxDriver driver, String uiTestId) throws Exception {
        var js = (JavascriptExecutor) driver;
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + uiTestId);
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-visibility")));
        Thread.sleep(1000);
        checkCheckBox(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input")));
        Thread.sleep(1000); //todo show-key-requestHost show-key-stimulatorTest
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-4")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-4")).sendKeys("www");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/be");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-1")).sendKeys("db");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);

        //https://www.baeldung.com/java-full-path-of-jar-from-class
        var version = SeleniumBase.getVersion();
        showMessage(driver, "Stopping be and gateway");
        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                                (psLine.contains("be-" + version) || psLine.contains("gateway-" + version))) ||
                        psLine.contains("org.h2.tools.Server") &&
                                !psLine.contains("globaltest"));
    }


    public static String cloneTo(FirefoxDriver driver, String sourceId, String destName) throws InterruptedException {

        Thread.sleep(1000);
        driver.get("http://www.local.test/plugins/recording/index.html");
        Thread.sleep(1000);
        for (var element : driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-0\"]"))) {
            if (element.getText().equalsIgnoreCase(sourceId)) {
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(() -> driver.findElement(By.id("grid-rowe-" + row + "-2")));
                Thread.sleep(1000);
                break;
            }
        }
        driver.findElement(By.id("newname")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("newname")).sendKeys(destName);
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(2000);

        for (var element : driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-2\"]"))) {
            if (element.getText().equalsIgnoreCase(destName)) {
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(() -> driver.findElement(By.id("grid-rowe-" + row + "-0")));
                Thread.sleep(1000);
                break;
            }
        }

        var result = driver.findElement(By.id("id")).getAttribute("value");
        return result;
    }

    public static void prepareGatewayNullTest(FirefoxDriver driver, String gatewayTestId) throws Exception {
        var js = (JavascriptExecutor) driver;
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + gatewayTestId);
        Thread.sleep(1000);

        var version = SeleniumBase.getVersion();

        showMessage(driver, "Stopping fe and starting gateway");
        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                                (psLine.contains("fe-" + version))) ||
                        psLine.contains("org.h2.tools.Server") &&
                                !psLine.contains("globaltest"));
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
        run(root, env, "gateway");


        scrollFind(driver, () -> driver.findElement(By.id("grid-visibility"))).click();
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input"))).click();
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(4) .form-check-input"))).click();
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-s-c-4")));
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-4")).sendKeys("www");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-1")).sendKeys("db");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/gat");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-seltostim")));
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Thread.sleep(1000);

        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).clear();
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-2")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-2")).sendKeys("true");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-setscript")));
        Thread.sleep(1000);

        driver.findElement(By.id("jsScriptPost")).clear();
        scrollFind(driver, () -> driver.findElement(By.id("jsScriptPost")));
        driver.findElement(By.id("jsScriptPost")).sendKeys("    var diffEngine = new org.kendar.xml.DiffInferrer();\n" +
                "    diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());\n" +
                "    if(expectedresponse.getStatusCode()!=response.getStatusCode()){\n" +
                "        throw \"Expected status code \"+expectedresponse.getStatusCode()+\" but received \"+response.getStatusCode();\n" +
                "    }");

        js.executeScript("document.getElementById('mod-save').click();");
        Thread.sleep(2000);

        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();

        //https://www.baeldung.com/java-full-path-of-jar-from-class

    }

    public static void prepareDbNullTest(FirefoxDriver driver, String dbNullTest) throws Exception {
        var js = (JavascriptExecutor) driver;
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + dbNullTest);
        Thread.sleep(1000);
        showMessage(driver, "Stopping gateway");
        var version = SeleniumBase.getVersion();
        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                                (psLine.contains("gateway-" + version))) ||
                        psLine.contains("org.h2.tools.Server") &&
                                !psLine.contains("globaltest"));


        scrollFind(driver, () -> driver.findElement(By.id("grid-visibility"))).click();
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input"))).click();
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(4) .form-check-input"))).click();
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-s-c-4")));
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-4")).sendKeys("www");
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/gat");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/be");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-seltostim")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Thread.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Thread.sleep(1000);
    }

    public static void prepareGatewayNullTestFail(FirefoxDriver driver, String gatewayFailTestId) throws Exception {
        showMessage(driver, "Setup a fail changing the expected data");
        doClick(() -> driver.findElement(By.id("grid-rowe-5-1")));
        Thread.sleep(1000);
        driver.findElement(By.linkText("[SELECTED 285]")).click();
        Thread.sleep(1000);
        //recording-ifr-content
        var frame= scrollFind(driver,()->driver.findElement(By.id("recording-ifr-content")));
        driver.switchTo().frame(frame);
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("RESDATA")));
        Thread.sleep(1000);
        driver.findElement(By.id("res_free_content")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("res_free_content")).
                sendKeys("[{\"id\":1,\"name\":\"John Doe\",\"role\":\"Doctor\",\"fail\":\"fail\"}]");
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("res-savechang")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("current-saveglobalchanges")));
        Thread.sleep(1000);
        driver.switchTo().defaultContent();
        Thread.sleep(1000);
    }
}

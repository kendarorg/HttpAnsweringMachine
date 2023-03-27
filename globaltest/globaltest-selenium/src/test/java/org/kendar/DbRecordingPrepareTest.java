package org.kendar;

import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.ProcessUtils;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashMap;
import java.util.Map;

import static org.kendar.SeleniumBase.*;

public class DbRecordingPrepareTest {
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());

    public static void prepareUiTest(ChromeDriver driver, String uiTestId) throws Exception {
        var js = (JavascriptExecutor) driver;
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + uiTestId);
        Sleeper.sleep(1000);
        scrollFind(driver,()->driver.findElement(By.id("scriptstab_0"))).click();
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-visibility")));
        Sleeper.sleep(1000);
        checkCheckBox(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input")));
        Sleeper.sleep(1000); //todo show-key-requestHost show-key-stimulatorTest
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-4")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-4")).sendKeys("www");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/be");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-1")).sendKeys("db");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);

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


    public static String cloneTo(ChromeDriver driver, String sourceId, String destName) throws InterruptedException {

        Sleeper.sleep(1000);
        driver.get("http://www.local.test/plugins/recording/index.html");
        Sleeper.sleep(1000);
        for (var element : driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-0\"]"))) {
            if (element.getText().equalsIgnoreCase(sourceId)) {
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(() -> driver.findElement(By.id("grid-rowe-" + row + "-2")));
                Sleeper.sleep(1000);
                break;
            }
        }
        doClick(() -> driver.findElement(By.id("newname")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("newname")).sendKeys(destName);
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(5000);

        for (var element : driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-2\"]"))) {
            if (element.getText().equalsIgnoreCase(destName)) {
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(() -> driver.findElement(By.id("grid-rowe-" + row + "-0")));
                Sleeper.sleep(1000);
                break;
            }
        }

        var result = driver.findElement(By.id("id")).getAttribute("value");
        return result;
    }

    public static void prepareGatewayNullTest(ChromeDriver driver, String gatewayTestId) throws Exception {
        var js = (JavascriptExecutor) driver;
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + gatewayTestId);
        Sleeper.sleep(1000);

        var version = SeleniumBase.getVersion();

        showMessage(driver, "Stopping fe and starting gateway");
        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        psLine.contains("org.h2.tools.Server") &&
                                !psLine.contains("globaltest"));

        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                                (psLine.contains("fe-" + version)||
                                        psLine.contains("gateway-" + version))) &&
                                !psLine.contains("globaltest"));
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
        run(root, env, "gateway");
        HttpChecker.checkForSite(60, "http://127.0.0.1:8090/api/v1/health")
                .noError().run();


        scrollFind(driver,()->driver.findElement(By.id("scriptstab_0"))).click();
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-visibility"))).click();
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input"))).click();
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(4) .form-check-input"))).click();
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-s-c-4")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-4")).sendKeys("www");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-1")).sendKeys("db");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/gat");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-seltostim")));
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Sleeper.sleep(1000);

        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).clear();
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-2")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-2")).sendKeys("true");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-setscript")));
        Sleeper.sleep(1000);

        driver.findElement(By.id("jsScriptPost")).clear();
        scrollFind(driver, () -> driver.findElement(By.id("jsScriptPost")));
        driver.findElement(By.id("jsScriptPost")).sendKeys("    var diffEngine = new org.kendar.xml.DiffInferrer();\n" +
                "    diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());\n" +
                "    if(expectedresponse.getStatusCode()!=response.getStatusCode()){\n" +
                "        throw \"Expected status code \"+expectedresponse.getStatusCode()+\" but received \"+response.getStatusCode();\n" +
                "    }");

        js.executeScript("document.getElementById('mod-save').click();");
        Sleeper.sleep(2000);

        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();

        //https://www.baeldung.com/java-full-path-of-jar-from-class

    }

    public static void prepareDbNullTest(ChromeDriver driver, String dbNullTest) throws Exception {
        var js = (JavascriptExecutor) driver;
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + dbNullTest);
        Sleeper.sleep(1000);
        showMessage(driver, "Stopping gateway");
        var version = SeleniumBase.getVersion();
        _processUtils.killProcesses((psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                                (psLine.contains("gateway-" + version))) ||
                        psLine.contains("org.h2.tools.Server") &&
                                !psLine.contains("globaltest"));

        scrollFind(driver,()->driver.findElement(By.id("scriptstab_0"))).click();
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-visibility"))).click();
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input"))).click();
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.cssSelector("tr:nth-child(4) .form-check-input"))).click();
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-s-c-4")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-4")).sendKeys("www");
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/gat");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/be");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-seltostim")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Sleeper.sleep(1000);
        scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
    }

    public static void prepareGatewayNullTestFail(ChromeDriver driver, String gatewayFailTestId) throws Exception {
        showMessage(driver, "Setup a fail changing the expected data");
        doClick(() -> driver.findElement(By.id("grid-rowe-5-1")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("scriptstab_1")));
        Sleeper.sleep(1000);
        //recording-ifr-content
        var frame= scrollFind(driver,()->driver.findElement(By.id("recording-ifr-content")));
        driver.switchTo().frame(frame);
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("curhttptab_4")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("res_free_content")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("res_free_content")).clear();
        driver.findElement(By.id("res_free_content")).
                sendKeys("[{\"id\":1,\"name\":\"John Doe\",\"fail\":\"Doctor\"}]");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("res-savechang")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("current-saveglobalchanges")));
        Sleeper.sleep(1000);
        driver.switchTo().defaultContent();
        Sleeper.sleep(1000);
    }
}

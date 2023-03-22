package org.kendar;

import org.apache.commons.lang3.NotImplementedException;
import org.kendar.globaltest.ProcessUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;
import java.util.function.Function;

import static org.kendar.SeleniumBase.*;

public class DbRecordingPrepareTest {
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());
    public static void prepareUiTest(FirefoxDriver driver, String uiTestId) throws Exception {
        var js = (JavascriptExecutor) driver;
        driver.get("http://www.local.test/plugins/recording/script.html?id="+uiTestId);
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("grid-visibility")));
        Thread.sleep(1000);
        checkCheckBox(driver,() -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        scrollFind(driver,()->driver.findElement(By.id("grid-s-c-4"))).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-4")).sendKeys("www");
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver,()->driver.findElement(By.id("grid-s-c-5"))).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).sendKeys("/int/be");
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);
        scrollFind(driver,()->driver.findElement(By.id("grid-s-c-1"))).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-s-c-1")).sendKeys("db");
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("recording-list-checkall")));
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("recording-list-delsel")));
        Thread.sleep(1000);

        //https://www.baeldung.com/java-full-path-of-jar-from-class
        var version = SeleniumBase.getVersion();
        _processUtils.killProcesses( (psLine) ->
                psLine.contains("java") &&
                        (psLine.contains("httpanswering") &&
                        (psLine.contains("be-"+version)||psLine.contains("gateway-"+version)))||
                                psLine.contains("org.h2.tools.Server") &&
                        !psLine.contains("globaltest"));
    }

    public static void prepareGatewayNullTest(FirefoxDriver driver, String gatewayTestId) {
        throw new NotImplementedException();
    }

    public static void prepareDbNullTest(FirefoxDriver driver, String dbNullTest) {
        throw new NotImplementedException();
    }

    public static String cloneTo(FirefoxDriver driver, String sourceId, String destName) throws InterruptedException {

        Thread.sleep(1000);
        driver.get("http://www.local.test/plugins/recording/index.html");
        Thread.sleep(1000);
        for(var element:driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-0\"]"))){
            if(element.getText().equalsIgnoreCase(sourceId)){
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(()->driver.findElement(By.id("grid-rowe-"+row+"-2")));
                Thread.sleep(1000);
                break;
            }
        }
        driver.findElement(By.id("newname")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("newname")).sendKeys(destName);
        Thread.sleep(1000);
        doClick(()->driver.findElement(By.id("mod-save")));
        Thread.sleep(2000);

        for(var element:driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-2\"]"))){
            if(element.getText().equalsIgnoreCase(destName)){
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(()->driver.findElement(By.id("grid-rowe-"+row+"-0")));
                Thread.sleep(1000);
                break;
            }
        }

        var result = driver.findElement(By.id("id")).getAttribute("value");
        return result;
    }
}

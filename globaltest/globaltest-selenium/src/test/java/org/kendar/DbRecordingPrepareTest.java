package org.kendar;

import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.kendar.SeleniumBase.doClick;

public class DbRecordingPrepareTest {
    public static void prepareUiTest(FirefoxDriver driver, String uiTestId) {

        throw new NotImplementedException();
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

package org.kendar.cucumber;

import io.cucumber.java.en.And;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.kendar.cucumber.Utils.*;

public class UiTestTasks {
    @And("^Prepare UI test '(.+)'$")
    public void prepareUiTest(String recordingName) throws Exception {
        var uiTestId = (String) Utils.getCache("recording_" + recordingName);
        var driver = (WebDriver) Utils.getCache("driver");
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + uiTestId);
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("scriptstab_0"))).click();
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-visibility")));
        Sleeper.sleep(1000);
        checkCheckBox(() -> driver.findElement(By.cssSelector("tr:nth-child(6) .form-check-input")));
        Sleeper.sleep(1000); //todo show-key-requestHost show-key-stimulatorTest
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("grid-s-c-4")), 100).click();
        Sleeper.sleep(1000);
        sendKeys(By.id("grid-s-c-4"),"www");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
        sendKeys(By.id("grid-s-c-5"),"/int/be");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        Sleeper.sleep(1000);
        sendKeys(By.id("grid-s-c-1"),"db");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
    }
}

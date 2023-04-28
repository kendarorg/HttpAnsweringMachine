package org.kendar.cucumber;

import io.cucumber.java.en.And;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import static org.kendar.cucumber.Utils.*;

public class NullGatewayTasks {
    @And("^Prepare NullGateway test '(.+)'$")
    public void prepareNullGatewayTest(String gatewayTestName) throws Exception {
        var gatewayTestId = (String) Utils.getCache("recording_" + gatewayTestName);
        var driver = (WebDriver) Utils.getCache("driver");
        navigateTo("http://www.local.test/plugins/recording/script.html?id=" + gatewayTestId);
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("scriptstab_0"))).click();
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("grid-visibility"))).click();
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("show-key-requestHost")));
        checkCheckBox(() -> driver.findElement(By.id("show-key-requestHost")));
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("show-key-stimulatorTest")));
        checkCheckBox(() -> driver.findElement(By.id("show-key-stimulatorTest")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-s-c-4")));
        Sleeper.sleep(1000);
        sendKeys(By.id("grid-s-c-4"),"www");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("grid-s-c-1")), 100).click();
        Sleeper.sleep(1000);
        sendKeys(By.id("grid-s-c-1"),"db");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        doClick(() -> driver.findElement(By.id("recording-list-delsel")));
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
        sendKeys(By.id("grid-s-c-5"),"/int/gat");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-seltostim")));
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Sleeper.sleep(1000);
    }

    @And("^Set NullGateway verification script for '(.+)'$")
    public void setNullGatewayVerificationScriptFor(String gatewayTestName) throws Exception {
        var gatewayTestId = (String) Utils.getCache("recording_" + gatewayTestName);
        var driver = (WebDriver) Utils.getCache("driver");
        var js = (JavascriptExecutor) driver;
        navigateTo("http://www.local.test/plugins/recording/script.html?id=" + gatewayTestId);
        Sleeper.sleep(1000);

        scrollFind(() -> driver.findElement(By.id("grid-s-c-5")), 100).click();
        Sleeper.sleep(1000);
        driver.findElement(By.id("grid-s-c-5")).clear();
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("grid-s-c-2")), 100).click();
        Sleeper.sleep(1000);
        sendKeys(By.id("grid-s-c-2"),"true");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-checkall")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-list-setscript")));
        Sleeper.sleep(1000);

        driver.findElement(By.id("jsScriptPost")).clear();
        scrollFind(() -> driver.findElement(By.id("jsScriptPost")));
        sendKeys(By.id("jsScriptPost"),"    var diffEngine = new org.kendar.xml.DiffInferrer();\n" +
                "    diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());\n" +
                "    if(expectedresponse.getStatusCode()!=response.getStatusCode()){\n" +
                "        throw \"Expected status code \"+expectedresponse.getStatusCode()+\" but received \"+response.getStatusCode();\n" +
                "    }");

        js.executeScript("document.getElementById('mod-save').click();");
        Sleeper.sleep(2000);

        scrollFind(() -> driver.findElement(By.id("grid-s-c-5")), 100).click();
    }

    @And("^Set NullGateway verification fail for '(.+)'$")
    public void setNullGatewayVerificationFailtFor(String gatewayTestName) throws Exception {
        var gatewayTestId = (String) Utils.getCache("recording_" + gatewayTestName);
        var driver = (WebDriver) Utils.getCache("driver");
        doClick(() -> driver.findElement(By.id("grid-rowe-5-1")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("scriptstab_1")));
        Sleeper.sleep(1000);
        //recording-ifr-content
        var frame = scrollFind(() -> driver.findElement(By.id("recording-ifr-content")));
        driver.switchTo().frame(frame);
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("curhttptab_4")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("res_free_content")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("res_free_content")).clear();
        sendKeys(By.id("res_free_content"),"[{\"id\":1,\"name\":\"John Doe\",\"fail\":\"Doctor\"}]");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("res-savechang")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("current-saveglobalchanges")));
        Sleeper.sleep(1000);
        driver.switchTo().defaultContent();
        Sleeper.sleep(1000);
    }
}

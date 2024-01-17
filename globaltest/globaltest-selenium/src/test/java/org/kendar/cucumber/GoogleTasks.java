package org.kendar.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.kendar.cucumber.Utils.*;

public class GoogleTasks {

    private static void acceptCookies(WebDriver driver) {
        try {
            WebElement el = null;
            try {
                el = scrollFind(() -> driver.findElement(By.xpath("//*[text()='Accetta tutto']")));
            } catch (Exception e) {

            }
            if (el == null) {
                el = scrollFind(() -> driver.findElement(By.xpath("//*[text()='Accept all']")));
            }
            el.click();
        } catch (Exception ex) {

        }
    }

    @Given("^The google home page$")
    public void theGoogleHomePage() throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");
        driver.get("https://www.google.com");
        Sleeper.sleep(1000);
        takeSnapShot();
        acceptCookies(driver);
        Sleeper.sleep(1000);
    }


    @When("^Creating filter to change google to bing$")
    public void creatingFilterToChangeGoogleToBing() {
        var driver = (WebDriver) Utils.getCache("driver");
        driver.get("http://www.local.test/index.html");
        org.kendar.globaltest.Sleeper.sleep(1000);
        takeSnapShot();
        doClick(() -> driver.findElement(By.linkText("JsFilter web")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("js-grid-addnew")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("createScriptName")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        sendKeys(By.id("createScriptName"), "GoogleHack");
        org.kendar.globaltest.Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.id("createScriptBt")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-0")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("CURRENT")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("phase")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        {
            WebElement dropdown = driver.findElement(By.id("phase"));
            dropdown.findElement(By.xpath("//option[. = 'POST_CALL']")).click();
        }
        org.kendar.globaltest.Sleeper.sleep(1000);
        driver.findElement(By.cssSelector("#phase > option:nth-child(6)")).click();
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("hostAddress")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        driver.findElement(By.id("hostAddress")).clear();
        sendKeys(By.id("hostAddress"), "www.google.com");
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("pathAddress")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        driver.findElement(By.id("pathAddress")).clear();
        sendKeys(By.id("pathAddress"), "/");
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("source")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        driver.findElement(By.id("source")).clear();
        sendKeys(By.id("source"), "var regex=/\\/images\\/branding\\/[_a-zA-Z0-9]+\\/[_a-zA-Z0-9]+\\/[_a-zA-Z0-9]+\\.png/gm;\n" +
                "var responseText = response.getResponseText()+\"\";\n" +
                "var changedText = responseText.replace(regex,'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c7/Bing_logo_%282016%29.svg/320px-Bing_logo_%282016%29.svg.png');\n" +
                "response.setResponseText(changedText);\n" +
                "return false;");
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("editfilter-save")));
        org.kendar.globaltest.Sleeper.sleep(1000);
    }
}

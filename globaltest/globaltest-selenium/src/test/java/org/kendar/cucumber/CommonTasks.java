package org.kendar.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.kendar.globaltest.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kendar.cucumber.Utils.*;
import static org.kendar.globaltest.LocalFileUtils.pathOf;


public class CommonTasks {

    private static ProcessUtils processUtils = new ProcessUtils(new HashMap<>());

    public static ProcessRunner run(String root, Map<String, String> env, String script) throws Exception {
        env.put("RUN_INLINE","true");
        return new ProcessRunner(env).
                asShell().
                withCommand(script + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar", "scripts")).
                runBackground();
    }

    @When("^Adding ssl for '(.+)'$")
    public void addingSslFor(String toAdd){
        var driver = (WebDriver)Utils.getCache("driver");
        driver.get("http://www.local.test/index.html");
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowc-2-0")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("ssl-sites-add")));
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("address")));
        driver.findElement(By.id("address")).sendKeys(toAdd);
        org.kendar.globaltest.Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("address")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
    }

    @When("^Adding dns for '(.+)'$")
    public void addingDnsFor(String toAdd){
        var driver = (WebDriver)Utils.getCache("driver");
        driver.get("http://www.local.test/index.html");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowc-0-0")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("MAPPINGS")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns-mappings-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("dns")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("dns")).sendKeys(toAdd);
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
    }

    @Given("^The page does not contains '(.+)'$")
    public void thePageDoesNotContains(String toFind){
        var driver = (JavascriptExecutor)Utils.getCache("driver");
        var text = driver.executeScript("return document.documentElement.outerHTML;").toString();
        assertFalse(text.contains(toFind));
        org.kendar.utils.Sleeper.sleep(1000);
    }

    @Then("^The page contains '(.+)'$")
    public void thePageContains(String toFind){
        var driver = (JavascriptExecutor)Utils.getCache("driver");
        var text = driver.executeScript("return document.documentElement.outerHTML;").toString();
        assertTrue(text.contains(toFind));
        org.kendar.utils.Sleeper.sleep(1000);
    }

    @Then("^Wait '(.+)' seconds$")
    public void wait(String seconds){
        var secs = Integer.parseInt(seconds);
        Sleeper.sleep(1000*secs);
    }

    @Then("^Start applications '(.+)'$")
    public void startApplications(String applications) throws Exception {
        var driver = (WebDriver)Utils.getCache("driver");
        Map<String, String> env = new HashMap<>();
        var root = getRootPath(CommonTasks.class);
        var apps = applications.split(",");
        for(var app:apps){
            run(root, env, app);
            showMessage("Started "+app);
        }
        Sleeper.sleep(1000);
    }

    @And("^Stop applications '(.+)'$")
    public void stopApplications(String applications) throws Exception {
        var apps = applications.split(",");
        var version = getVersion();
        for(var app:apps){
            processUtils.killProcesses((psLine) ->
                    psLine.contains("java") &&
                            (psLine.contains("httpanswering") &&
                                    (psLine.contains(app+"-" + version))) &&
                            !psLine.contains("globaltest"));
            showMessage("Terminated "+app);
        }
        Sleeper.sleep(1000);
    }
    @And("^Wait for '(.+)' to be ready calling '(.+)' for '(.+)' seconds$")
    public void waitForAppToBeReadyCallingAddrForXSeconds(String app,String url,String secs) throws Exception{
        var seconds = Integer.parseInt(secs);
        HttpChecker.checkForSite(seconds, url)
                .noError().run();
    }
}

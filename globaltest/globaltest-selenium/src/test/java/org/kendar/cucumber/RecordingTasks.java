package org.kendar.cucumber;

import io.cucumber.java.en.And;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.kendar.SeleniumBase.doClick;
import static org.kendar.cucumber.Utils.*;


public class RecordingTasks {

    @And("^Navigate calendar ui$")
    public void navigateCalendarUi() throws Exception{
        var driver = (WebDriver)Utils.getCache("driver");
        driver.get("http://www.sample.test/");
        Sleeper.sleep(2000);
        doClick(() -> driver.findElement(By.id("appoint-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("role")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("role")).sendKeys("Doctor");
        Sleeper.sleep(1000);
        driver.findElement(By.id("name")).sendKeys("John Doe");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("appoint-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("description")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("description")).sendKeys("Visit");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-1")));
        doClick(() -> driver.findElement(By.cssSelector(".row")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Employees")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-0")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("name")));
        driver.findElement(By.id("name")).clear();
        Sleeper.sleep(1000);
        driver.findElement(By.id("name")).sendKeys("Jane Doe");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-1")));
        Sleeper.sleep(1000);
    }

    @And("^Stop recording '(.+)'$")
    public void stopRecording(String recordingName) throws Exception{
        var driver = (WebDriver)Utils.getCache("driver");
        var recordingId = Utils.getCache("recording_"+recordingName);
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + recordingId);
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-stop")));
        Sleeper.sleep(3000);
        doClick(() -> driver.findElement(By.id("recording-list-reload")));
        Sleeper.sleep(1000);
    }
    @And("^Start calendar recording '(.+)'$")
    public void startCalendarRecording(String recordingName) throws Exception{
        var driver = (WebDriver)Utils.getCache("driver");
        doClick(() -> driver.findElement(By.linkText("Replayer web")));
        Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.id("main-recording-addnew")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("createScriptName")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("createScriptName")).sendKeys(recordingName);
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("createScriptBt"))).click();
        Sleeper.sleep(2000);

        driver.get("http://www.local.test/plugins/recording/script.html?id=1");

        try {
            Sleeper.sleep(1000);
            scrollFind( () -> driver.findElement(By.id("scriptstab_0"))).click();
        }catch (Exception ex){

        }
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("extdbname"))).click();
        driver.findElement(By.id("extdbname")).clear();
        Sleeper.sleep(1000);
        driver.findElement(By.id("extdbname")).sendKeys("be");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("description")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("description")).sendKeys("Full recording sample");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Sleeper.sleep(1000);
        showMessage("Starting recording");
        doClick(() -> driver.findElement(By.id("recording-startrecord")));
        Sleeper.sleep(1000);
        var result = driver.findElement(By.id("id")).getAttribute("value");
        Utils.setCache("recording_Main",result);
    }
}

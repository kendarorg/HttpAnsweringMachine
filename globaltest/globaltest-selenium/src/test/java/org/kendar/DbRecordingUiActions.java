package org.kendar;

import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.kendar.SeleniumBase.doClick;
import static org.kendar.SeleniumBase.setupSize;

public class DbRecordingUiActions {

    public static void fullNavigation(ChromeDriver driver) throws InterruptedException {
        Sleeper.sleep(1000);
        driver.get("http://www.sample.test/");
        Sleeper.sleep(2000);
        setupSize(driver);
        Sleeper.sleep(1000);
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
}

package org.kendar;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.kendar.SeleniumBase.doClick;

public class DbRecordingUiActions {

    public static void fullNavigation(FirefoxDriver driver) throws InterruptedException {
        Thread.sleep(1000);
        driver.get("http://www.sample.test/");
        Thread.sleep(1000);
        driver.manage().window().setSize(new Dimension(1024, 1024));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("appoint-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("role")));
        Thread.sleep(1000);
        driver.findElement(By.id("role")).sendKeys("Doctor");
        Thread.sleep(1000);
        driver.findElement(By.id("name")).sendKeys("John Doe");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("appoint-add")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("description")));
        Thread.sleep(1000);
        driver.findElement(By.id("description")).sendKeys("Visit");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-1")));
        doClick(() -> driver.findElement(By.cssSelector(".row")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Employees")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-0")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("name")));
        driver.findElement(By.id("name")).clear();
        Thread.sleep(1000);
        driver.findElement(By.id("name")).sendKeys("Jane Doe");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Thread.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-1")));
        Thread.sleep(1000);
    }
}

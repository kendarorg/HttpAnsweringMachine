package org.kendar;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.firefox.FirefoxDriver;

public class DbRecordingUiActions {

    public static void fullNavigation(FirefoxDriver driver) throws InterruptedException {

        driver.get("http://www.sample.test/");
        Thread.sleep(1000);
        driver.manage().window().setSize(new Dimension(1024, 1024));
        Thread.sleep(1000);
        driver.findElement(By.id("appoint-add")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("role")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("role")).sendKeys("Doctor");
        Thread.sleep(1000);
        driver.findElement(By.id("name")).sendKeys("John Doe");
        driver.findElement(By.id("mod-save")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-rowe-0-2")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("appoint-add")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("description")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("description")).sendKeys("Visit");
        driver.findElement(By.id("mod-save")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-rowe-0-2")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-rowe-0-2")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-rowe-0-1")).click();
        driver.findElement(By.cssSelector(".row")).click();
        Thread.sleep(1000);
        driver.findElement(By.linkText("Employees")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-rowe-0-0")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("name")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("name")).sendKeys("Jane Doe");
        driver.findElement(By.id("mod-save")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("grid-rowe-0-1")).click();
        Thread.sleep(1000);
    }
}

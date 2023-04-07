package org.kendar.cucumber;

import io.cucumber.java.en.And;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.kendar.cucumber.Utils.*;


public class RecordingBackgroundTasks {
    @And("^Initialized calendar rewrite url and H2 Db$")
    public void initializeRewrites() throws Exception {
        var driver = (WebDriver)Utils.getCache("driver");
        driver.get("http://www.local.test/index.html");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("Url/Db Rewrites")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/gateway.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8090");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8090");
        Sleeper.sleep(1000);
        checkCheckBox(() -> driver.findElement(By.id("force")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("when")).sendKeys("http://localhost/int/be.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8100");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8100");
        Sleeper.sleep(1000);
        checkCheckBox(() -> driver.findElement(By.id("force")));
        doClick(() -> driver.findElement(By.id("mod-save")));
        doClick(() -> driver.findElement(By.id("webprx-gird-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("when")));
        driver.findElement(By.id("when")).sendKeys("http://www.sample.test");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("where")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("where")).sendKeys("http://127.0.0.1:8080");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("test")));
        driver.findElement(By.id("test")).sendKeys("127.0.0.1:8080");
        Sleeper.sleep(1000);
        checkCheckBox(() -> driver.findElement(By.id("force")));
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.linkText("JDBC PROXIES")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("jdbcprx-grid-addnew")));
        doClick(() -> driver.findElement(By.id("driver")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("driver")).sendKeys("org.h2.Driver");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringR")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("connectionStringR")).sendKeys("jdbc:h2:tcp://localhost:9123/./data/be;MODE=MYSQL;");
        doClick(() -> driver.findElement(By.id("loginR")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("loginR")).sendKeys("sa");
        Sleeper.sleep(1000);
        driver.findElement(By.cssSelector("span > div")).click();
        doClick(() -> driver.findElement(By.id("passwordR")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("passwordR")).sendKeys("sa");
        Sleeper.sleep(1000);
        checkCheckBox(() -> driver.findElement(By.id("active")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("connectionStringL")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("connectionStringL")).sendKeys("be");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("loginL")));
        driver.findElement(By.id("loginL")).sendKeys("login");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("passwordL")));
        Sleeper.sleep(1000);
        driver.findElement(By.id("passwordL")).sendKeys("password");
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("mod-save"))).click();
        Sleeper.sleep(1000);
    }
}

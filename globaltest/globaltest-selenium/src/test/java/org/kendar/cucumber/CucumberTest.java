package org.kendar.cucumber;

import io.cucumber.java.BeforeStep;
import io.cucumber.junit.platform.engine.Cucumber;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;

@Cucumber
/*@Suite
@IncludeEngines("cucumber")
/*@SelectClasspathResource("org/kendar/ham")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.kendar.ham")*/
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.kendar.cucumber")
public class CucumberTest {
    @BeforeStep
    public void beforeStep() {
        var driver = (WebDriver) Utils.getCache("driver");
        var js = (JavascriptExecutor)driver;
    }
}
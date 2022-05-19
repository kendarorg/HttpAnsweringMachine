package org.kendar.ham;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import io.cucumber.java.an.Cuan;
import io.cucumber.junit.platform.engine.Cucumber;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("classpath")
public class CucumberIntegrationIT {
}

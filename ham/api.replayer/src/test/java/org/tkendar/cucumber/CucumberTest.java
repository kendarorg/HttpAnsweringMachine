package org.tkendar.cucumber;


import io.cucumber.junit.platform.engine.Cucumber;
import org.junit.platform.suite.api.ConfigurationParameter;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;

@Cucumber
/*@Suite
@IncludeEngines("cucumber")
/*@SelectClasspathResource("org/kendar/ham")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.kendar.ham")*/
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.kendar.cucumber")
public class CucumberTest {

}

package org.kendar.cucumber;

import io.cucumber.java.en.Given;
import org.kendar.DbRecordingSetupTest;
import org.kendar.globaltest.LocalFileUtils;
import org.kendar.globaltest.ProcessRunner;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

import static org.kendar.cucumber.Utils.getRootPath;
import static org.kendar.cucumber.Utils.showMessage;
import static org.kendar.globaltest.LocalFileUtils.pathOf;

public class DbTasks {
    @Given("^Start h2 db$")
    public void startH2Db() throws Exception {
        var driver = (WebDriver)Utils.getCache("driver");
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
        new ProcessRunner(env).
                asShell().
                withCommand("rundb" + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar")).
                runBackground();
        showMessage("Started H2 Database");
        Sleeper.sleep(1000);
    }
}

package org.kendar;

import org.apache.commons.lang3.NotImplementedException;
import org.kendar.globaltest.LocalFileUtils;
import org.kendar.globaltest.ProcessRunner;
import org.kendar.globaltest.ProcessUtils;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.kendar.SeleniumBase.getRootPath;
import static org.kendar.globaltest.LocalFileUtils.pathOf;

public class DbRecordingSetupTest {
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());

    private static final Function<String, Boolean> findHamProcesses = (psLine) ->
            psLine.contains("java") &&
                    psLine.contains("httpanswering") &&
                    !psLine.contains("globaltest");

    private static ProcessRunner run(String root, Map<String, String> env, String script) throws Exception {
        return new ProcessRunner(env).
                asShell().
                withCommand(script + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar", "scripts")).
                runBackground();
    }
    public static void startup(FirefoxDriver driver) throws Exception {
        var root = getRootPath(DbRecordingSetupTest.class);
        Map<String, String> env = new HashMap<>();
        var processRunners = new ArrayList<ProcessRunner>();
        processRunners.add(new ProcessRunner(env).
                asShell().
                withCommand("rundb" + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar")).
                runBackground());

        Thread.sleep(1000);
        processRunners.add(run(root, env, "ham"));
        processRunners.add(run(root, env, "gateway"));
        processRunners.add(run(root, env, "fe"));
        processRunners.add(run(root, env, "bedbham"));

        Thread.sleep(5000);
        _processUtils.killProcesses(findHamProcesses);
    }

    public static void fullNavigation(FirefoxDriver driver) {
        throw new NotImplementedException();
    }

    public static void analyzeRecording(FirefoxDriver driver, String idRecording) {
        throw new NotImplementedException();
    }

    public static String downloadRecording(FirefoxDriver driver) {
        throw new NotImplementedException();
    }

    public static String startRecording(FirefoxDriver driver, String idRecording) {
        throw new NotImplementedException();
    }

    public static void stopAction(FirefoxDriver driver, String idRecording) {
        throw new NotImplementedException();
    }

    public static String prepareUiTest(FirefoxDriver driver, String recordingData,String recordingId) {
        throw new NotImplementedException();
    }

    public static String prepareGatewayNullTest(FirefoxDriver driver, String recordingData,String recordingId) {
        throw new NotImplementedException();
    }

    public static String prepareFakeDbTest(FirefoxDriver driver, String recordingData,String recordingId) {
        throw new NotImplementedException();
    }

    public static void startPlaying(FirefoxDriver driver,String idRecording) {
        throw new NotImplementedException();
    }

    public static void startNullPlaying(FirefoxDriver driver,String idRecording) {
        throw new NotImplementedException();
    }
    public static void loadResults(FirefoxDriver driver,String idRecording) {
        throw new NotImplementedException();
    }
}

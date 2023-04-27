package org.kendar.cucumber;

import io.cucumber.java.en.Given;
import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.ProcessRunner;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.kendar.cucumber.Utils.deleteDirectory;
import static org.kendar.cucumber.Utils.getRootPath;


public class BackgroundTasks {


    @Given("^Cache initialized$")
    public void cacheInitialized() throws Exception {
        Utils.getCache().clear();
    }
    @Given("^Ham started$")
    public void hamStarted() throws Exception {
        Utils.initShutdownHook();
        var caller = BackgroundTasks.class;
        try {
            if (HttpChecker.checkForSite(5, "http://127.0.0.1/api/dns/lookup/test").noError().run()) {
                return;
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        deleteDirectory(Path.of(getRootPath(caller), "release","data").toFile());
        deleteDirectory(Path.of(getRootPath(caller), "release", "calendar", "data").toFile());
        Files.deleteIfExists(Path.of(getRootPath(caller), "release", "calendar", "be.mv.db"));
        Files.deleteIfExists(Path.of(getRootPath(caller), "release", "data", "ham.mv.db"));
        var java = "java";
        var agentPath = Path.of(getRootPath(caller), "ham", "api.test", "org.jacoco.agent-0.8.8-runtime.jar");
        var jacocoExecPath = Path.of(getRootPath(caller), "ham", "api.test", "target", "jacoco_selenium.exec");
        var externalJsonPath = Path.of(getRootPath(caller), "ham", "test.external.json").toString();
        var libsPath = Path.of(getRootPath(caller), "ham", "libs").toString();
        var appPathRootPath = Path.of(getRootPath(caller), "ham", "app", "target");

        if (!appPathRootPath.toFile().exists()) {
            throw new Exception("WRONG STARTING PATH " + appPathRootPath);
        }
        File[] matchingFiles = appPathRootPath.toFile().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name == null) return false;
                return name.startsWith("app-") && name.endsWith(".jar");
            }
        });
        var appPathRoot = Path.of(matchingFiles[0].getAbsolutePath()).toString();

        var pr = new ProcessRunner(new ConcurrentHashMap<>()).
                asShell().
                withCommand(java).
                withParameter("-Djsonconfig=" + externalJsonPath).
                withParameter("-Dloader.path=" + libsPath).
                withParameter("-Dham.tempdb="+Path.of(getRootPath(caller), "release","data","tmp")).
                withParameter("-Dperformance.watcher.interval=0").
                withParameter("-Dloader.main=org.kendar.Main").
                withParameter("-javaagent:" + agentPath + "=destfile=" + jacocoExecPath + ",includes=org.kendar.**").
                withParameter("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9863").
                withParameter("-jar").
                withParameter(appPathRoot);
        try {
            pr.runBackground();
            if (!HttpChecker.checkForSite(60, "http://127.0.0.1/api/dns/lookup/test").run()) {
                throw new Exception("NOT STARTED");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}

package org.tkendar.ham;

import org.apache.commons.lang3.SystemUtils;
import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.LocalFileUtils;
import org.kendar.globaltest.ProcessRunner;
import org.kendar.globaltest.ProcessUtils;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class HamStarter {

    private static Logger logger = LoggerFactory.getLogger(HamStarter.class);

    private static boolean showTrace = false;
    private static Thread realThread;

    public static void showTrace() {
        showTrace = true;
    }

    public static class ThreadAndProc {
        public Thread thread;
        public Process process;
        public ArrayList<Supplier<Boolean>> trace;
    }

    private static String getRootPath(Class<?> caller) {
        final File jarFile =
                new File(caller.getProtectionDomain().getCodeSource().getLocation().getPath());
        var path = Path.of(jarFile.getAbsolutePath());
        return path.getParent()    //target
                .getParent()    //api.ham
                .getParent()    //ham
                .getParent().toAbsolutePath().toString();
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        if (!directoryToBeDeleted.exists()) {
            return true;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private static String findJava() {
        return "java";

    }

    private static ConcurrentHashMap<String, ThreadAndProc> processes = new ConcurrentHashMap<>();


    public static boolean shutdownHookInitialized = false;



    private static void initShutdownHook() {
        if (shutdownHookInitialized) return;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                var pu = new ProcessUtils(new HashMap<>());
                try {

                    HttpChecker.checkForSite(60, "http://127.0.0.1/api/shutdown").noError().run();
                    pu.sigtermProcesses((str)-> str.contains("-Dloader.main=org.kendar.Main"));
                } catch (Exception e) {

                }
            }

        });
    }

    public static void killHam(Class<?> caller){
        var pu = new ProcessUtils(new HashMap<>());
        try {

            HttpChecker.checkForSite(5, "http://127.0.0.1/api/shutdown").noError().run();
            //pu.sigtermProcesses((str)-> str.toLowerCase(Locale.ROOT).contains("-dloader.main=org.kendar.main"));
        } catch (Exception e) {

        }
    }

    public static void runHamJar(Class<?> caller) throws HamTestException {
        try {
            if(HttpChecker.checkForSite(5, "http://127.0.0.1/api/dns/lookup/test").noError().run()){
                return;
            }
        } catch (Exception e) {
            throw new HamTestException(e);
        }
        var commandLine = new ArrayList<String>();

        deleteDirectory(Path.of(getRootPath(caller),"data","tmp").toFile());
        var java = findJava();
        var agentPath = Path.of(getRootPath(caller), "ham", "api.test", "org.jacoco.agent-0.8.8-runtime.jar");
        var jacocoExecPath = Path.of(getRootPath(caller), "ham", "api.test", "target", "jacoco_starter.exec");
        var externalJsonPath = Path.of(getRootPath(caller), "ham", "test.external.json").toString();
        var libsPath = Path.of(getRootPath(caller), "ham", "libs").toString();
        var appPathRootPath = Path.of(getRootPath(caller), "ham", "app", "target");

        if (!appPathRootPath.toFile().exists()) {
            throw new HamTestException("WRONG STARTING PATH " + appPathRootPath);
        }
        File[] matchingFiles = appPathRootPath.toFile().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name == null) return false;
                return name.startsWith("app-") && name.endsWith(".jar");
            }
        });
        var appPathRoot = Path.of(matchingFiles[0].getAbsolutePath()).toString();
        initShutdownHook();

        var pr = new ProcessRunner(new ConcurrentHashMap<>()).
                asShell().
                withCommand(java).
                withParameter("-Djsonconfig=" + externalJsonPath).
                withParameter("-Dloader.path=" + libsPath).
                withParameter("-Dham.tempdb=data/tmp").
                withParameter("-Dloader.main=org.kendar.Main").
                withParameter("-javaagent:" + agentPath + "=destfile=" + jacocoExecPath + ",includes=org.kendar.**").
                withParameter("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9863").
                withParameter("-jar").
                withParameter(appPathRoot);
        try {
            pr.runBackground();
            if(!HttpChecker.checkForSite(60, "http://127.0.0.1/api/dns/lookup/test").run()){
                throw new Exception("NOT STARTED");
            }
        } catch (Exception e) {
            throw new HamTestException(e);
        }

    }
}

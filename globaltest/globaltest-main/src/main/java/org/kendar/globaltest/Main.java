package org.kendar.globaltest;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class Main {
    private static HashMap<String, String> env;
    private static ProcessKiller _processKiller;
    private static HttpChecker _httpChecker;
    private static Function<String, Boolean> findHamProcesses = (psLine) ->
                    psLine.contains("java") &&
                    psLine.contains("httpanswering") &&
                    !psLine.contains("globaltest");
    private static void doOrExit(ExceptionSupplier<Boolean> b) throws Exception {
        if (!b.get()) doExit(1);
    };

    private static void killApacheLogger() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "error");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "error");
        System.setProperty("log4j.logger.org.apache.http", "error");
        System.setProperty("log4j.logger.org.apache.http.wire", "error");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");
    }

    public static void correctAllCrLf(String directoryName) {
        File directory = new File(directoryName);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".sh")) {
                    try {
                        var content = Files.readString(Path.of(file.getAbsolutePath()));
                        if (content.indexOf("\r\n") > 0) {
                            var result = content.replace("\r\n", "\n");
                            Files.writeString(Path.of(file.getAbsolutePath()), result);
                        }
                    } catch (IOException e) {

                    }
                }
            } else if (file.isDirectory()) {
                if (file.getName().startsWith(".")) {
                    continue;
                }
                correctAllCrLf(file.getAbsolutePath());
            }
        }
    }


    private static void doExit(int i) {
        try {
            if (i == 0) {
                _processKiller.killProcesses(findHamProcesses);
            } else {
                var scanner = new Scanner(System.in); // create scanner
                LogWriter.warn("");
                LogWriter.warn("there was an error");       // prompt u
                LogWriter.warn("Would you like to leave the processes running? (y/n default)");       // prompt user
                var result = scanner.next().toLowerCase(Locale.ROOT);
                if (result.isEmpty() || result.equalsIgnoreCase("n")) {
                    _processKiller.killProcesses(findHamProcesses);
                }
                exit(i);
            }
        } catch (Exception ex) {
            exit(1);
        }
    }

    private static String ext() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return ".bat";
        }
        return ".sh";
    }


    private static String pathOf(String first, String... pars) {
        return Path.of(first, pars).toString();
    }


    private static void runBuild(String buildDir, String releasePath, String script) throws Exception {
        LogWriter.info("BEG " + script);
        var runDir = buildDir;
        var runFile = pathOf(runDir, script + ext());
        var logDir = releasePath;

        new ProcessRunner(env).withCommand(runFile).withStartingPath(runDir).run();

        for (var file : new File(logDir).listFiles(((dir, name) -> name.toLowerCase().endsWith(".log")))) {
            try (var reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                var hasError = false;
                while (line != null) {
                    if (line.toLowerCase(Locale.ROOT).contains("ERROR")) {
                        LogWriter.errror("[%s] %s", script, line);
                        hasError = true;
                    }
                    // read next line
                    line = reader.readLine();
                }
                if (hasError) {
                    doExit(1);
                }

            }
        }


        LogWriter.info("END " + script);
    }


    private static void cleanDirectory(String path) throws Exception {
        File directory = new File(path);
        if (directory.exists()) {
            FileUtils.cleanDirectory(directory);
        }
    }

    private static String findCommand(String command) {
        try {
            ConcurrentLinkedQueue<String> storage = new ConcurrentLinkedQueue<>();
            new ProcessRunner(env).withCommand("whereis").withParameter(command).withStorage(storage).run();
            var lines = storage.peek().split("\\s+");
            for (var line : lines) {
                if (line.indexOf("bin") > 0) {
                    return line;
                }
            }
        } catch (Exception ex) {
        }
        return command;
    }

    private static void chmodExec(String dir) throws Exception {
        if (SystemUtils.IS_OS_WINDOWS) return;
        if (!Files.exists(Path.of(dir))) {
            Files.createDirectories(Path.of(dir));
        }

        new ProcessRunner(env).asShell().withParameter("chmod +x *.sh").withStartingPath(dir).withNoOutput().run();
    }

    private static void extract(String startingPath, String name) throws Exception {
        File initialFile = new File(startingPath + File.separatorChar + name + ".tar.gz");
        var in = new FileInputStream(initialFile);
        var BUFFER_SIZE = 64000;
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                /** If the entry is a directory, create the directory. **/
                if (entry.isDirectory()) {
                    var entryName = entry.getName();
                    if (entryName.equalsIgnoreCase("./")) continue;
                    entryName = startingPath + entryName.substring(1);
                    File f = new File(entryName);
                    boolean created = f.mkdir();
                    if (!created) {
                        LogWriter.errror("Unable to create directory '%s', during extraction of archive contents.", f.getAbsolutePath());
                        doExit(1);
                    }
                } else {
                    int count;
                    byte data[] = new byte[BUFFER_SIZE];
                    var entryName = entry.getName();
                    entryName = startingPath + entryName.substring(1);
                    var fos = new FileOutputStream(entryName, false);
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                    }
                }
            }

            LogWriter.info("Untar completed successfully!");
        }
    }








    private static void testCalendarSample(String calendarPath) throws Exception {
        LogWriter.info("Testing calendar/scripts/be");
        start(pathOf(calendarPath, "scripts"), "be").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://127.0.0.1:8100/api/v1/health"));
        _processKiller.killProcesses(findHamProcesses);

        LogWriter.info("Testing calendar/scripts/fe");
        start(pathOf(calendarPath, "scripts"), "fe").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://127.0.0.1:8080/api/v1/health"));
        _processKiller.killProcesses(findHamProcesses);

        LogWriter.info("Testing calendar/scripts/gateway");
        start(pathOf(calendarPath, "scripts"), "gateway").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://127.0.0.1:8090/api/v1/health"));
        _processKiller.killProcesses(findHamProcesses);

        LogWriter.info("Testing calendar/scripts/ham");
        start(pathOf(calendarPath, "scripts"), "ham").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.local.test/api/health", "127.0.0.1", 1081));
        _processKiller.killProcesses(findHamProcesses);


        LogWriter.info("Testing calendar/rundb");
        start(calendarPath, "rundb").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://localhost:8082"));
        LogWriter.info("Testing calendar/scripts/bedb");
        start(pathOf(calendarPath, "scripts"), "bedb").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://127.0.0.1:8100/api/v1/health"));
        _processKiller.killProcesses(findHamProcesses);
    }

    private static void testLocalHam(String releasePath) throws Exception {
        LogWriter.info("Testing ham/local.run");
        start(pathOf(releasePath, "ham"), "local.run").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://127.0.0.1/api/health"));
        _processKiller.killProcesses(findHamProcesses);

        LogWriter.info("Testing ham/proxy.run");
        start(pathOf(releasePath, "ham"), "proxy.run").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.local.test/api/health", "127.0.0.1", 1081));
        _processKiller.killProcesses(findHamProcesses);
    }

    private static void applyReleasePermissions(String releasePath) throws Exception {
        chmodExec(pathOf(releasePath, "ham"));
        chmodExec(pathOf(releasePath, "simpledns"));
        chmodExec(pathOf(releasePath, "calendar"));
        chmodExec(pathOf(releasePath, "calendar", "scripts"));
    }

    private static void buildDeploymentArtifacts(String startingPath, String hamVersion, String buildDir, String releasePath) throws Exception {
        cleanDirectory(releasePath);

        runBuild(buildDir, releasePath, "build_release");
        extract(pathOf(startingPath, "release"), "ham-" + hamVersion);
        runBuild(buildDir, releasePath, "build_release_samples");
        extract(pathOf(startingPath, "release"), "ham-samples-" + hamVersion);
    }


    private static void handleDockerErrors(String a, Process p) {
        if (a.toLowerCase(Locale.ROOT).startsWith("error") || a.toLowerCase(Locale.ROOT).startsWith("couldn't connect")) {
            LogWriter.errror("");
            LogWriter.errror( a);
            p.destroy();
            doExit(1);
        }
    }


    private static void handleDockTagged(String s, Process process) {
        if (s.toLowerCase(Locale.ROOT).startsWith("successfully tagged")) {
            LogWriter.info("" + s);
        }
    }


    private static void buildDockerImages(String buildDir) throws Exception {
        LogWriter.info("Building docker");
        start(buildDir, "build_docker", Main::handleDockerErrors, Main::handleDockTagged).run();
        LogWriter.info("Building docker samples");
        start(buildDir, "build_docker_samples", Main::handleDockerErrors, Main::handleDockTagged).run();
    }

    private static void testDockerCalendarAndQuotesSamples(String dockerIp, String samplesDir) throws Exception {
        LogWriter.info("Starting composer calendar");
        startComposer(pathOf(samplesDir, "calendar", "hub_composer"), "docker-compose-local.yml", "down").runBackground();
        Thread.sleep(3000);
        startComposer(pathOf(samplesDir, "calendar", "hub_composer"), "docker-compose-local.yml", "up", Main::handleDockerErrors).runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.local.test/api/health", dockerIp, 1081));
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.sample.test/api/v1/health", dockerIp, 1081));
        doOrExit(()->_httpChecker.checkForSite(60, "http://gateway.sample.test/api/v1/health", dockerIp, 1081));
        doOrExit(()->_httpChecker.checkForSite(60, "http://be.sample.test/api/v1/health", dockerIp, 1081));
        startComposer(pathOf(samplesDir, "calendar", "hub_composer"), "docker-compose-local.yml", "down").runBackground();


        LogWriter.info("Starting composer quotes");
        startComposer(pathOf(samplesDir, "quotes", "hub_composer"), "docker-compose-local.yml", "down").runBackground();
        Thread.sleep(3000);
        startComposer(pathOf(samplesDir, "quotes", "hub_composer"), "docker-compose-local.yml", "up", Main::handleDockerErrors).runBackground();

        Thread.sleep(3000);
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.local.test/api/health", dockerIp, 1081));
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.quotes.test/api/health/index.php", dockerIp, 1081));
        startComposer(pathOf(samplesDir, "quotes", "hub_composer"), "docker-compose-local.yml", "down").runBackground();

        Thread.sleep(3000);
    }

    private static void handleRunErrors(String a, Process process) {
        if (a.toLowerCase(Locale.ROOT).startsWith("[error]") || a.equalsIgnoreCase("Error starting applicationcontext")) {
            LogWriter.errror("");
            LogWriter.errror(a);
            process.destroy();
            doExit(1);
        }
    }


    private static void testCalendarSampleFull(String calendarPath) throws Exception {
        LogWriter.info("Testing calendar/runcalendar");
        start(pathOf(calendarPath), "runcalendar").runBackground();
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.local.test/api/health", "127.0.0.1", 1081));
        doOrExit(()->_httpChecker.checkForSite(60, "http://www.sample.test/api/v1/health", "127.0.0.1", 1081));
        doOrExit(()->_httpChecker.checkForSite(60, "http://localhost/int/gateway.sample.test/api/v1/health", "127.0.0.1", 1081));
        doOrExit(()->_httpChecker.checkForSite(60, "http://localhost/int/be.sample.test/api/v1/health", "127.0.0.1", 1081));
        _processKiller.killProcesses(findHamProcesses);

        LogWriter.info("Starting calendar");
    }

    public static void main(String[] args) throws Exception {

        try {

            killApacheLogger();

            var startingPath = System.getenv("STARTING_PATH");
            var hamVersion = System.getenv("HAM_VERSION");
            var dockerIp = System.getenv("DOCKER_IP");
            var dockerHost = System.getenv("DOCKER_HOST");

            //hamVersion = "4.1.5";

            //startingPath = "C:\\Data\\Github\\HttpAnsweringMachine";
            //dockerIp="192.168.56.2";

            //startingPath = "/Users/edaros/Personal/Github/HttpAnsweringMachine";
            //dockerIp="192.168.1.40";

            //dockerHost="tcp://"+dockerIp+":23750";


            env = new HashMap<String, String>();
            env.put("STARTING_PATH", startingPath);
            env.put("HAM_VERSION", hamVersion);
            env.put("DOCKER_IP", dockerIp);
            env.put("DOCKER_HOST", dockerHost);
            _processKiller = new ProcessKiller(env);
            _processKiller.killProcesses(findHamProcesses);
            _httpChecker = new HttpChecker();

            correctAllCrLf(startingPath);

            var buildDir = pathOf(startingPath, "scripts", "build");
            var samplesDir = pathOf(startingPath, "samples");
            var releasePath = pathOf(startingPath, "release");
            var calendarPath = pathOf(releasePath, "calendar");


            buildDeploymentArtifacts(startingPath, hamVersion, buildDir, releasePath);
            testAndGenerateJacoco(startingPath, hamVersion, buildDir, releasePath);
            applyReleasePermissions(releasePath);
            testLocalHam(releasePath);
            testCalendarSample(calendarPath);
            testCalendarSampleFull(calendarPath);
            buildDockerImages(buildDir);
            testDockerCalendarAndQuotesSamples(dockerIp, samplesDir);

            _processKiller.killProcesses(findHamProcesses);
            exit(0);
        } catch (Exception ex) {
            LogWriter.errror(ex);
            doExit(2);
        }

    }

    private static void testAndGenerateJacoco(String startingPath, String hamVersion, String buildDir, String releasePath) throws Exception {
        LogWriter.info("Unit test ham & report");
        start(pathOf(startingPath, "scripts", "globaltest"), "test.run", Main::handleRunErrors).run();
        if (SystemUtils.IS_OS_WINDOWS) {
            doOrExit(()->_httpChecker.checkForSite(60, "http://127.0.0.1/api/shutdown"));
        } else {
            _processKiller.sigtermProcesses(findHamProcesses);
        }
        var path = Path.of(startingPath, "ham", "api.test", "target", "test_run_starter.exec");
        var now = System.currentTimeMillis();
        var end = now + 5 * 60 * 1000;

        LogWriter.info("Waiting for coverage data");
        while ((!Files.exists(path) || Files.size(path) == 0) && end > System.currentTimeMillis()) {
            System.out.print(".");
            Thread.sleep(1000);
        }
        if (!Files.exists(path) || Files.size(path) == 0) {
            LogWriter.errror("Error loading jacoco reports " + path);
            doExit(1);
        } else {
            LogWriter.info("OK");
        }

        _processKiller.sigtermProcesses(findHamProcesses);
        start(pathOf(startingPath, "scripts", "globaltest"), "test.jacoco", Main::handleRunErrors).run();
    }


    private static ProcessRunner start(String dir, String script,
                                  BiConsumer<String,Process> ...biConsumers) throws Exception {

        var pr = new ProcessRunner(env).
                asShell().
                withParameter(script + ext()).
                withStartingPath(dir).
                withNoOutput();
        if (biConsumers.length > 0) {
            pr.withErr(biConsumers[0]);
        }
        if (biConsumers.length > 1) {
            pr.withOut(biConsumers[1]);
        }
        return pr;
    }



    private static ProcessRunner startComposer(String dir, String composer, String sense, BiConsumer<String, Process>... biConsumers) throws Exception {

        var pr = new ProcessRunner(env).asShell().
                withParameter("docker-compose").
                withParameter("-f").withParameter(composer).
                withParameter(sense).withStartingPath(dir).withNoOutput();
        if (biConsumers.length > 0) {
            pr.withErr(biConsumers[0]);
        }
        if (biConsumers.length > 1) {
            pr.withOut(biConsumers[1]);
        }
        return pr;
    }


}


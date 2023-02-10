package org.kendar.globaltest;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class Main {
    private static HashMap<String, String> env;

    private static void killApacheLogger() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "error");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "error");
        System.setProperty("log4j.logger.org.apache.http", "error");
        System.setProperty("log4j.logger.org.apache.http.wire", "error");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");
    }


    private static void doExit(int i) {
        try {
            if (i == 0) {
                killAllHamProcesses();
            } else {
                var scanner = new Scanner(System.in); // create scanner
                System.out.println("");
                System.out.println("[WARN] there was an error");       // prompt u
                System.out.println("[WARN] Would you like to leave the processes running? (y/n default)");       // prompt user
                var result = scanner.next().toLowerCase(Locale.ROOT);
                if (result.isEmpty() || result.equalsIgnoreCase("n")) {
                    killAllHamProcesses();
                }
                exit(i);
            }
        }catch (Exception ex){
            exit(1);
        }
    }

    private static String ext(){
        if(SystemUtils.IS_OS_WINDOWS){
            return ".bat";
        }
        return ".sh";
    }
    private static void killAllHamProcesses() throws Exception {
        if(!SystemUtils.IS_OS_WINDOWS) {
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("ps").
                    withParameter("-ef").
                    withStorage(queue).
                    run();
            var allJavaProcesses = queue.stream().filter(a->{
                return a.toLowerCase(Locale.ROOT).contains("java") &&
                        a.toLowerCase(Locale.ROOT).contains("httpanswering")
                        && !a.toLowerCase(Locale.ROOT).contains("globaltest")
                        ;
            }).collect(Collectors.toList());
            for(var javaHam:allJavaProcesses){
                var spl = javaHam.trim().split("\\s+");
                var pid = spl[1];
                new ProcessRunner(env).
                        withCommand("kill").
                        withParameter("-9").
                        withParameter(pid).
                        withNoOutput().
                        run();
            }
        }else{
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("wmic").
                    withParameter("process").
                    withParameter("list").
                    withParameterPlain("/format:csv").
                    withStorage(queue).
                    run();
            var allJavaProcesses = queue.stream().filter(a->{
                return a.toLowerCase(Locale.ROOT).contains("java") &&
                        a.toLowerCase(Locale.ROOT).contains("httpanswering")
                        && !a.toLowerCase(Locale.ROOT).contains("globaltest");
            }).collect(Collectors.toList());
            for(var javaHam:allJavaProcesses){
                //var str="XPS15-KENDAR,,XPS15-KENDAR,System Idle Process,,,0,0,,20510591875000,,,System Idle Process,Microsoft Windows 11 Pro|C:\\WINDOWS|\\Device\\Harddisk0\\Partition3,0,0,9,60,0,60,8192,8,0,61440,0,1,0,1,0,0,0,0,,,32,0,8192,10.0.22621,8192,0,0";

                var spl = javaHam.trim().split(",");
                var pid = spl[spl.length-17];
                    new ProcessRunner(env).
                            withCommand("taskkill").
                            withParameter("/f").
                            withParameter("/pid").
                            withParameter(pid).
                            withNoOutput().
                            run();

            }
        }
    }

    private static String pathOf(String first,String ... pars){
        return Path.of(first,pars).toString();
    }


    private static void runBuild(String buildDir,String releasePath,String script) throws Exception {
        System.out.println("[INFO] BEG "+script);
        var runDir = buildDir;
        var runFile = pathOf(runDir,script+ext());
        var logDir = releasePath;

        new ProcessRunner(env).
                withCommand(runFile).
                withStartingPath(runDir).
                run();

        for(var file: new File(logDir).listFiles(((dir, name) -> name.toLowerCase().endsWith(".log")))){
            try(var reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                var hasError = false;
                while (line != null) {
                    if (line.toLowerCase(Locale.ROOT).contains("ERROR")) {
                        System.err.printf("[ERROR] [%s] %s" ,script,line);
                        hasError=true;
                    }
                    // read next line
                    line = reader.readLine();
                }
                if(hasError){
                    doExit(1);
                }

            }
        }


        System.out.println("[INFO] END "+script);
    }


    private static void cleanDirectory(String path)throws Exception {
        File directory = new File(path);
        if(directory.exists()) {
            FileUtils.cleanDirectory(directory);
        }
    }

    private static String findCommand(String command){
        try {
            ConcurrentLinkedQueue<String> storage = new ConcurrentLinkedQueue<>();
            new ProcessRunner(env).
                    withCommand("whereis").
                    withParameter(command).
                    withStorage(storage).
                    run();
            var lines = storage.peek().split("\\s+");
            for (var line : lines) {
                if (line.indexOf("bin") > 0) {
                    return line;
                }
            }
        }catch (Exception ex){}
        return command;
    }

    private static void chmodExec(String dir) throws Exception {
        if(SystemUtils.IS_OS_WINDOWS)return;
        if(!Files.exists(Path.of(dir))){
            Files.createDirectories(Path.of(dir));
        }

        new ProcessRunner(env).
                withCommand("bash").
                withParameter("-c").
                withParameter("chmod +x *.sh").
                withNoOutput().
                run();
    }

    private static void extract(String startingPath, String name) throws Exception {
        File initialFile = new File(startingPath+File.separatorChar+name+".tar.gz");
        var in = new FileInputStream(initialFile);
        var BUFFER_SIZE = 64000;
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                /** If the entry is a directory, create the directory. **/
                if (entry.isDirectory()) {
                    var entryName = entry.getName();
                    if(entryName.equalsIgnoreCase("./"))continue;
                    entryName = startingPath+entryName.substring(1);
                    File f = new File(entryName);
                    boolean created = f.mkdir();
                    if (!created) {
                        System.err.printf("[ERROR] Unable to create directory '%s', during extraction of archive contents.\n",
                                f.getAbsolutePath());
                        doExit(1);
                    }
                } else {
                    int count;
                    byte data[] = new byte[BUFFER_SIZE];
                    var entryName = entry.getName();
                    entryName = startingPath+entryName.substring(1);
                    var fos = new FileOutputStream(entryName, false);
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                    }
                }
            }

            System.out.println("[INFO] Untar completed successfully!");
        }
    }


    private static void checkForSite(int seconds, String url) throws Exception {
        checkForSite(seconds,url,null,-1);
    }



    private static void checkForSite(int seconds, String url,String proxyUrl,int proxyPort) throws Exception{
        var now =System.currentTimeMillis();
        var end = now+seconds*1000;
        System.out.printf("[INFO] Testing for %d seconds %s: ",seconds,url);
        while(end>System.currentTimeMillis()) {
            System.out.print(".");
            if(proxyUrl!=null){
                var proxy = new HttpHost(proxyUrl, proxyPort, "http");
                var routePlanner = new DefaultProxyRoutePlanner(proxy);
                try (var httpclient = HttpClients.custom()
                        .setRoutePlanner(routePlanner)
                        .build()) {
                    var httpget = new HttpGet(url);
                    var httpresponse = httpclient.execute(httpget);
                    if (httpresponse.getStatusLine().getStatusCode() == 200) {
                        System.out.print("OK\n");
                        return;
                    }
                } catch (Exception ex) {
                    //NOP
                }
            }else {

                try (var httpclient = HttpClients.createDefault()) {
                    var httpget = new HttpGet(url);
                    var httpresponse = httpclient.execute(httpget);
                    if (httpresponse.getStatusLine().getStatusCode() == 200) {
                        System.out.print("OK\n");
                        return;
                    }
                } catch (Exception ex) {
                    //NOP
                }
            }
            Thread.sleep(1000);
        }
        System.err.println("[ERROR] testing " + url);
        doExit(1);
    }







    private static void testCalendarSample(String calendarPath) throws Exception {
        System.out.println("[INFO] Testing calendar/scripts/be");
        startBackground(pathOf(calendarPath, "scripts"), "be");
        checkForSite(60, "http://127.0.0.1:8100/api/v1/health");
        killAllHamProcesses();

        System.out.println("[INFO] Testing calendar/scripts/fe");
        startBackground(pathOf(calendarPath, "scripts"), "fe");
        checkForSite(60, "http://127.0.0.1:8080/api/v1/health");
        killAllHamProcesses();

        System.out.println("[INFO] Testing calendar/scripts/gateway");
        startBackground(pathOf(calendarPath, "scripts"), "gateway");
        checkForSite(60, "http://127.0.0.1:8090/api/v1/health");
        killAllHamProcesses();

        System.out.println("[INFO] Testing calendar/scripts/ham");
        startBackground(pathOf(calendarPath, "scripts"), "ham");
        checkForSite(60, "http://www.local.test/api/health", "127.0.0.1", 1081);
        killAllHamProcesses();


        System.out.println("[INFO] Testing calendar/rundb");
        startBackground(calendarPath, "rundb");
        checkForSite(60, "http://localhost:8082");
        System.out.println("[INFO] Testing calendar/scripts/bedb");
        startBackground(pathOf(calendarPath, "scripts"), "bedb");
        checkForSite(60, "http://127.0.0.1:8100/api/v1/health");
        killAllHamProcesses();
    }

    private static void testLocalHam(String releasePath) throws Exception {
        System.out.println("[INFO] Testing ham/local.run");
        startBackground(pathOf(releasePath, "ham"), "local.run");
        checkForSite(60, "http://127.0.0.1/api/health");
        killAllHamProcesses();

        System.out.println("[INFO] Testing ham/proxy.run");
        startBackground(pathOf(releasePath, "ham"), "proxy.run");
        checkForSite(60, "http://www.local.test/api/health", "127.0.0.1", 1081);
        killAllHamProcesses();
    }

    private static void applyReleasePermissions(String releasePath) throws Exception {
        chmodExec(pathOf(releasePath, "ham"));
        chmodExec(pathOf(releasePath, "simpledns"));
        chmodExec(pathOf(releasePath, "calendar"));
        chmodExec(pathOf(releasePath, "calendar", "scripts"));
    }

    private static void buildDeploymentArtifacts(String startingPath, String hamVersion, String buildDir, String releasePath) throws Exception {
        cleanDirectory(releasePath);

        runBuild(buildDir, releasePath,"build_release");
        extract(pathOf(startingPath, "release"), "ham-" + hamVersion);
        runBuild(buildDir, releasePath,"build_release_samples");
        extract(pathOf(startingPath, "release"), "ham-samples-" + hamVersion);
    }

    private static void startBackground(String dir, String script,
                                        BiConsumer<String,Process> ...biConsumers) throws Exception {
        if(SystemUtils.IS_OS_WINDOWS){
            var pr =new ProcessRunner(env).
                    withCommand("cmd").
                    withParameter("/C").
                    withParameter(script+ext()).
                    withStartingPath(dir).
                    withNoOutput();
            if(biConsumers.length>0) {
                pr.withErr(biConsumers[0]);
            }
            if(biConsumers.length>1) {
                pr.withOut(biConsumers[1]);
            }
            pr.runBackground();
        }else {
            var pr = new ProcessRunner(env).
                    withCommand("bash").
                    withParameter("-c").
                    withParameter("./"+script+ext()).
                    withStartingPath(dir).
                    withNoOutput();
            if(biConsumers.length>0) {
                pr.withErr(biConsumers[0]);
            }
            if(biConsumers.length>1) {
                pr.withOut(biConsumers[1]);
            }
            pr.runBackground();
        }
    }

    private static void startWait(String dir, String script,
                                  BiConsumer<String,Process> ...biConsumers) throws Exception {

        if(SystemUtils.IS_OS_WINDOWS){
            var pr =new ProcessRunner(env).
                    withCommand("cmd").
                    withParameter("/C").
                    withParameter(script+ext()).
                    withStartingPath(dir).
                    withNoOutput();
            if(biConsumers.length>0) {
                pr.withErr(biConsumers[0]);
            }
            if(biConsumers.length>1) {
                pr.withOut(biConsumers[1]);
            }
            pr.run();
        }else {
            var pr =new ProcessRunner(env).
                    withCommand("bash").
                    withParameter("-c").
                    withParameter("./" +script+ext()).
                    withStartingPath(dir).
                    withNoOutput();
            if(biConsumers.length>0) {
                pr.withErr(biConsumers[0]);
            }
            if(biConsumers.length>1) {
                pr.withOut(biConsumers[1]);
            }
            pr.run();
        }
    }



    private static void handleDockerErrors(String a, Process p) {
        if(a.toLowerCase(Locale.ROOT).startsWith("error")){
            System.err.println("");
            System.err.println("[ERROR] "+ a);
            p.destroy();
            doExit(1);
        }
    }


    private static void handleDockTagged(String s, Process process) {
        if(s.toLowerCase(Locale.ROOT).startsWith("successfully tagged")){
            System.out.println("[INFO] "+s);
        }
    }



    private static void buildDockerImages(String buildDir) throws Exception {
        System.out.println("[INFO] Building docker");
        startWait(buildDir,"build_docker",
                Main::handleDockerErrors,
                Main::handleDockTagged);
        System.out.println("[INFO] Building docker samples");
        startWait(buildDir,"build_docker_samples",
                Main::handleDockerErrors,
                Main::handleDockTagged);
    }

    private static void testDockerCalendarAndQuotesSamples(String dockerIp, String samplesDir) throws Exception {
        System.out.println("[INFO] Starting composer calendar");
        startComposer(pathOf(samplesDir,"calendar","hub_composer"),
                "docker-compose-local.yml",
                "down");
        Thread.sleep(3000);
        startComposer(pathOf(samplesDir,"calendar","hub_composer"),
                "docker-compose-local.yml",
                "up",
                Main::handleRunErrors);
        checkForSite(60, "http://www.local.test/api/health", dockerIp, 1081);
        checkForSite(60, "http://www.sample.test/api/v1/health", dockerIp, 1081);
        checkForSite(60, "http://gateway.sample.test/api/v1/health", dockerIp, 1081);
        checkForSite(60, "http://be.sample.test/api/v1/health", dockerIp, 1081);
        startComposer(pathOf(samplesDir,"calendar","hub_composer"),
                "docker-compose-local.yml",
                "down");


        System.out.println("[INFO] Starting composer quotes");
        startComposer(pathOf(samplesDir,"quotes","hub_composer"),
                "docker-compose-local.yml",
                "down");
        Thread.sleep(3000);
        startComposer(pathOf(samplesDir,"quotes","hub_composer"),
                "docker-compose-local.yml",
                "up",
                Main::handleRunErrors);
        checkForSite(60, "http://www.local.test/api/health", dockerIp, 1081);
        checkForSite(60, "http://www.quotes.test/api/health/index.php", dockerIp, 1081);
        startComposer(pathOf(samplesDir,"quotes","hub_composer"),
                "docker-compose-local.yml",
                "down");
    }

    private static void handleRunErrors(String a, Process process) {
        if(a.toLowerCase(Locale.ROOT).startsWith("[error]")){
            System.err.println("");
            System.err.println( a);
            process.destroy();
            doExit(1);
        }
    }

    private static void startComposer(String dir,String composer,String sense,
                                        BiConsumer<String,Process> ...biConsumers) throws Exception {
        if(SystemUtils.IS_OS_WINDOWS){
            var pr =new ProcessRunner(env).
                    withCommand("cmd").
                    withParameter("/C").
                    withParameter("docker-compose").
                    withParameter("-f").
                    withParameter(composer).
                    withParameter(sense).
                    withStartingPath(dir)
                    .withNoOutput();
            if(biConsumers.length>0) {
                pr.withErr(biConsumers[0]);
            }
            if(biConsumers.length>1) {
                pr.withOut(biConsumers[1]);
            }
            pr.runBackground();
        }else {
            var pr = new ProcessRunner(env).
                    withCommand("bash").
                    withParameter("-c").
                    withParameter("docker-compose").
                    withParameter("-f").
                    withParameter(composer).
                    withParameter(sense).
                    withStartingPath(dir).
                    withNoOutput();
            if(biConsumers.length>0) {
                pr.withErr(biConsumers[0]);
            }
            if(biConsumers.length>1) {
                pr.withOut(biConsumers[1]);
            }
            pr.runBackground();
        }
    }



    private static void testCalendarSampleFull(String calendarPath) throws Exception {
        System.out.println("[INFO] Testing calendar/runcalendar");
        startBackground(pathOf(calendarPath), "runcalendar");
        checkForSite(60, "http://www.local.test/api/health","127.0.0.1",1081);
        checkForSite(60, "http://www.sample.test/api/v1/health","127.0.0.1",1081);
        checkForSite(60, "http://localhost/int/gateway.sample.test/api/v1/health","127.0.0.1",1081);
        checkForSite(60, "http://localhost/int/be.sample.test/api/v1/health","127.0.0.1",1081);
        killAllHamProcesses();

        System.out.println("[INFO] Starting calendar");
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


            env = new HashMap<String,String>();
            env.put("STARTING_PATH",startingPath);
            env.put("HAM_VERSION",hamVersion);
            env.put("DOCKER_IP",dockerIp);
            env.put("DOCKER_HOST",dockerHost);
            killAllHamProcesses();

            var buildDir = pathOf(startingPath,"scripts","build");
            var samplesDir = pathOf(startingPath,"samples");
            var releasePath = pathOf(startingPath, "release");
            var calendarPath = pathOf(releasePath, "calendar");


            buildDeploymentArtifacts(startingPath, hamVersion, buildDir, releasePath);
            applyReleasePermissions(releasePath);
            testLocalHam(releasePath);
            testCalendarSample(calendarPath);
            testCalendarSampleFull(calendarPath);
            buildDockerImages(buildDir);
            testDockerCalendarAndQuotesSamples(dockerIp, samplesDir);

            killAllHamProcesses();
        }catch (Exception ex){
            System.err.println(ex);
            doExit(2);
        }

    }


}


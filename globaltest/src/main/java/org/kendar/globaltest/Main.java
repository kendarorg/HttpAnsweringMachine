package org.kendar.globaltest;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class Main {
    public void test() throws Exception {
        var queue = new ConcurrentLinkedQueue<String>();
        new ProcessRunner().
                withCommand("ps").
                withParameter("-ef").
                withStorage(queue).
                run();
        for(var item:queue){
            System.out.println(item);
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
            new ProcessRunner().
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
                new ProcessRunner().
                        withCommand("kill").
                        withParameter("-9").
                        withParameter(pid).
                        withNoOutput().
                        run();
            }
        }else{
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner().
                    withCommand("wmic").
                    withParameter("process").
                    withParameter("list").
                    withParameter("/format:csv").
                    withStorage(queue).
                    run();
            var allJavaProcesses = queue.stream().filter(a->{
                return a.toLowerCase(Locale.ROOT).contains("java") &&
                        a.toLowerCase(Locale.ROOT).contains("httpanswering")
                        && !a.toLowerCase(Locale.ROOT).contains("globaltest");
            }).collect(Collectors.toList());
            for(var javaHam:allJavaProcesses){
                var str="XPS15-KENDAR,,XPS15-KENDAR,System Idle Process,,,0,0,,20510591875000,,,System Idle Process,Microsoft Windows 11 Pro|C:\\WINDOWS|\\Device\\Harddisk0\\Partition3,0,0,9,60,0,60,8192,8,0,61440,0,1,0,1,0,0,0,0,,,32,0,8192,10.0.22621,8192,0,0";

                var spl = str.trim().split(",");
                var pid = spl[spl.length-17];
                new ProcessRunner().
                        withCommand("kill").
                        withParameter("-9").
                        withParameter(pid).
                        withNoOutput().
                        run();
            }
        }
    }



    private static void runBuild(String startingPath,String script) throws Exception {
        System.out.println("[INFO] BEG "+script);
        var runDir = startingPath+File.separatorChar+"scripts"+File.separatorChar+"build";
        var runFile = runDir+File.separatorChar+script+ext();
        var logDir = startingPath+File.separatorChar+"release";
        new ProcessRunner().
                withCommand(runFile).
                withStartingPath(runDir).
                run();
        for(var file: new File(logDir).listFiles(((dir, name) -> name.toLowerCase().endsWith(".log")))){
            try(var reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();

                while (line != null) {
                    if (line.toLowerCase(Locale.ROOT).contains("ERROR")) {
                        System.out.println("[ERROR] " + script);
                        exit(1);
                    }
                    // read next line
                    line = reader.readLine();
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

    private static void chmodExec(String dir) throws Exception {
        if(SystemUtils.IS_OS_WINDOWS)return;
        new ProcessRunner().
                withCommand("chmod").
                withParameter("+x").
                withParameter("*.sh").
                withStartingPath(dir).
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
                        System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n",
                                f.getAbsolutePath());
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

            System.out.println("Untar completed successfully!");
        }
    }


    public static void main(String[] args) throws Exception {
        var startingPath = System.getenv("STARTING_PATH");
        var hamVersion = System.getenv("HAM_VERSION");


        startingPath = "C:\\Data\\Github\\HttpAnsweringMachine";
        hamVersion="4.1.5";
        killAllHamProcesses();
        cleanDirectory(startingPath+File.separatorChar+"release");

        runBuild(startingPath,"build_release");
        extract(startingPath+File.separatorChar+"release","ham-"+hamVersion);
        runBuild(startingPath,"build_release_samples");
        extract(startingPath+File.separatorChar+"release","ham-samples-"+hamVersion);

        chmodExec(startingPath+File.separatorChar+"release"+File.separatorChar+"ham");
        chmodExec(startingPath+File.separatorChar+"release"+File.separatorChar+"simpledns");
        chmodExec(startingPath+File.separatorChar+"release"+File.separatorChar+"calendar");
        chmodExec(startingPath+File.separatorChar+"release"+File.separatorChar+"calendar"+File.separatorChar+"scripts");

    }
}


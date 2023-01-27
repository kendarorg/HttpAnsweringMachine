package org.kendar.ham;

import org.apache.commons.lang3.SystemUtils;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class HamStarter {

    private static Logger logger = LoggerFactory.getLogger(HamStarter.class);

    private static boolean showTrace = false;
    private static Thread realThread;

    public static void showTrace(){
        showTrace = true;
    }
    public static class ThreadAndProc{
        public Thread thread;
        public Process process;
        public ArrayList<Supplier<Boolean>> trace;
    }

    private static String getRootPath(Class<?> caller){
        final File jarFile =
                new File(caller.getProtectionDomain().getCodeSource().getLocation().getPath());
        var path = Path.of(jarFile.getAbsolutePath());
        return path.getParent()    //target
                .getParent()    //api.ham
                .getParent()    //ham
                .getParent().toAbsolutePath().toString();
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        if(!directoryToBeDeleted.exists()){
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
    private static String findJava(){
        return "java";
        /*
        var possible = System.getProperty("sun.boot.library.path");

        if(possible.endsWith(File.separator)){
            possible = possible.substring(0,possible.length()-1);
        }
        if(possible.endsWith("lib")){
            possible = possible.substring(0,possible.length()-3)+
                "bin"+
                File.separator+
                "java";
        }else if(possible.endsWith("bin")){
            possible = possible.substring(0,possible.length()-3)+
                    File.separator+
                    "java";
        }

        if(!Files.exists(Path.of(possible)) && !Files.exists(Path.of(possible+".exe"))){
            throw new RuntimeException("Missing java executable");

        }

        return possible;*/
    }
    private static ConcurrentHashMap<String,ThreadAndProc> processes = new ConcurrentHashMap<>();


    private static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return result.toString();
    }

    public static boolean shutdownHookInitialized =false;
    public static void runJar(List<String> command, String jarDir, Supplier<Boolean>... expected) throws HamTestException {

        var someError = false;
        var commandIndex = String.join(" ",command);
        for(var i=expected.length-1;i>=0;i--){
            if(!expected[i].get()){
                someError = true;
            }
        }
        if(!someError) return;
        var internalExpected = new ArrayList<>(Arrays.asList(expected));
        initShutdownHook();
        try {
            if(processes.containsKey(commandIndex)){
                var process = processes.get(commandIndex);
                if(process!=null) {
                    if (process.process.isAlive()) {
                        return;
                    }
                    process.thread = null;
                }
                processes.remove(commandIndex);
            }

            processes.computeIfAbsent(commandIndex,(cm)->{
                try {

                    var ntpc = new ThreadAndProc();
                    var pb = new ProcessBuilder(command);
                    pb.directory(new File(jarDir));

                    ntpc.process = pb.start();//Runtime.getRuntime().exec(command);
                    ntpc.trace = new ArrayList<>(internalExpected);
                    ntpc.thread = new Thread(new Runnable() {
                        public void run() {

                            /*try {
                                try (BufferedReader input =
                                             new BufferedReader(new
                                                     InputStreamReader(ntpc.process.getInputStream()))) {
                                    String line;
                                    while((line = input.readLine()) != null) {
                                        System.out.println(line);
                                    }
                                    while (ntpc.process.isAlive()) {
                                        while((line = input.readLine()) != null) {
                                            System.out.println(line);
                                        }
                                    }
                                }
                            }catch (IOException | BufferOverflowException ex){

                            }*/

                            InputStream fromProcess = ntpc.process.getInputStream();
                            InputStream fromError = ntpc.process.getErrorStream();

                            int x;

                            try {
                                while((x = fromProcess.read()) != -1)
                                    System.out.print((char)x);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                while((x = fromError.read()) != -1)
                                    System.err.print((char)x);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            int exit = ntpc.process.exitValue();
                            System.out.println("Exited with code " + exit);
                            //BufferedReader input = new BufferedReader(new InputStreamReader(ntpc.process.getInputStream()));


                        }
                    });
                    ntpc.thread.start();
                    System.out.println("STARTING "+commandIndex);

                    Sleeper.sleep(100);

                    //
                    /*while(ntpc.thread.isAlive()){
                        Sleeper.sleep(100);
                    }*/
                    return ntpc;
                }catch (Exception ex){
                    return null;
                }
            });

            var ntpc = processes.get(commandIndex);

            if(!ntpc.thread.isAlive()){
                ntpc.process.destroy();
                throw new RuntimeException("ERROR STARTING APP");
            }
            while(!ntpc.trace.isEmpty()){
                for(var i=ntpc.trace.size()-1;i>=0;i--){
                    if(ntpc.trace.get(i).get()){
                        ntpc.trace.remove(i);
                    }
                }
                Sleeper.sleep(100);
            }
            System.out.println("STARTED");
        }catch (Exception ex){
            throw new HamTestException("Unable to start "+command);
        }
    }

    private static void initShutdownHook() {
        if(shutdownHookInitialized)return;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                // place your code here
                for(var proc : processes.values()){
                    System.out.println("DESTROYING "+proc.process);
                    try {
                        var res =getHTML("http://127.0.0.1/api/shutdown");
                        //Thread.sleep(60*000);
                    }catch (Exception ex){}
                    //proc.process.destroy();



                }
            }

        });
    }
    public static void runHamJarCommand(Class<?> caller) throws HamTestException {
        var commandLine = new ArrayList<String>();
        commandLine.add(findJava());

        var agentPath = Path.of(getRootPath(caller),"ham","api.test","org.jacoco.agent-0.8.8-runtime.jar");
        var jacocoExecPath = Path.of(getRootPath(caller),"ham","api.test","target","jacoco_starter.exec");
        var externalJsonPath  =Path.of(getRootPath(caller),"ham","test.external.json").toString();
        commandLine.add("-Djsonconfig="+externalJsonPath);
        var libsPath  =Path.of(getRootPath(caller),"ham","libs").toString();
        commandLine.add("-Dloader.path="+libsPath);
        commandLine.add("-Dham.tempdb=data/tmp");
        commandLine.add("-Dloader.main=org.kendar.Main");
        if(!SystemUtils.IS_OS_WINDOWS) {
            commandLine.add("-javaagent:" + agentPath + "=destfile=" + jacocoExecPath + ",includes=org.kendar.**");
            commandLine.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9863");
        }else{
            commandLine.add("\"-javaagent:" + agentPath + "=destfile=" + jacocoExecPath + ",includes=org.kendar.**\"");
            commandLine.add("\"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9863\"");
        }

        //commandLine.add("\"-javaagent:"+agentPath+"=destfile="+jacocoExecPath+"\"");



        var appPathRootPath = Path.of(getRootPath(caller),"ham","app","target");

        if(!appPathRootPath.toFile().exists()){
            throw new HamTestException("WRONG STARTING PATH "+appPathRootPath);
        }
        File[] matchingFiles = appPathRootPath.toFile().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if(name == null) return false;
                return name.startsWith("app-") && name.endsWith(".jar");
            }
        });
        var appPathRoot  =Path.of(matchingFiles[0].getAbsolutePath()).toString();
        /*try {
            Files.copy(Path.of(appPathRoot),Path.of(appPathRoot+".tmp"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new HamTestException(e);
        }*/
        commandLine.add("-jar");
        //-javaagent:/opt/coverage/lib/jacocoagent.jar=destfile=/opt/coverage/jacoco.exec,include=org.kendar.*
        //
        commandLine.add(appPathRoot);
        var jarDir= Path.of(matchingFiles[0].getAbsolutePath()).getParent().toAbsolutePath().toString();
        //processBuilder.directory(new File("src"));
        runJar(commandLine,jarDir,()-> {
            deleteDirectory(Path.of(getRootPath(caller),"jsplugins").toFile());
            return true;
        },()-> {
            try {
                var result = getHTML("http://127.0.0.1/api/dns/lookup/test");
                return true;
            } catch (Exception ex) {
                return false;
            }
        });

    }
//
//    public static void runHamJarInternal(Class<?> caller) throws HamTestException {
//        if(realThread!=null && realThread.isAlive())return;
////        var commandLine = new ArrayList<String>();
////        commandLine.add(findJava());
//
//        var externalJsonPath  =Path.of(getRootPath(caller),"ham","test.external.json").toString();
//        //commandLine.add("-Djsonconfig="+externalJsonPath);
//        var libsPath  =Path.of(getRootPath(caller),"ham","libs").toString();
////        commandLine.add("-Dloader.path="+libsPath);
////        commandLine.add("-Dloader.main=org.kendar.Main");
//        var rootPath  =Path.of(getRootPath(caller),"ham").toString();
//        System.setProperty("user.dir", rootPath);
//        System.setProperty("jsonconfig",externalJsonPath);
//        System.setProperty("loader.main","org.kendar.Main");
//        System.setProperty("loader.path",libsPath);
//        System.setProperty("loader.home",rootPath);
//        //loader.home
//        try {
//
//            /*try {
//                var starter = Class.forName("org.kendar.Main");
//            }catch (Exception xx){}
//            var starter = Class.forName("org.kendar.Main");*/
//            //var starter = PropertiesLauncher.class;
//
//            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
//                    (hostname, sslSession) -> true);
//
//            SpringApplicationBuilder uws = new SpringApplicationBuilder(OtherMain.class)
//                    //.lazyInitialization(true)
//                    .properties("loader.path=" + libsPath,
//                            "jsonconfig=" + externalJsonPath,
//                            "loader.home=" + rootPath+"/target",
//                            "loader.main=org.kendar.Main");
//
//            realThread = new Thread(()->{
//                uws.run();
//            });
//            realThread.start();
//
//            var ready = false;
//            while(!ready){
//                try {
//                    URL url = new URL("http://localhost/api/health");
//                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                    con.setRequestMethod("GET");
//                    int status = con.getResponseCode();
//                    ready = true;
//                }catch (Exception ex){
//
//                }
//            }
//            Thread.sleep(1000);
//            ///api/health
//        }catch (Exception ex){
//            throw new HamTestException(ex);
//        }
//    }


    public static void runHamJar(Class<?> caller) throws HamTestException {
        //runHamJarInternal(caller);
        runHamJarCommand(caller);
    }
}

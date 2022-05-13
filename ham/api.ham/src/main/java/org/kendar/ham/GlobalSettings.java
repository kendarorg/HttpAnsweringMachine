package org.kendar.ham;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalSettings {
    public static class ThreadAndProc{
        public Thread thread;
        public Process process;
        public HashSet<String> trace;
    }
    public static HamBasicBuilder builder(){
        return HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1",1080)
                .withDns("127.0.0.1");
    }

    private static String getRootPath(){
        final File jarFile =
                new File(GlobalSettings.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        var path = Path.of(jarFile.getAbsolutePath());
        return path.getParent()    //target
                .getParent()    //api.ham
                .getParent()    //ham
                .getParent().toAbsolutePath().toString();
    }
    private static ConcurrentHashMap<String,ThreadAndProc> processes = new ConcurrentHashMap<>();
    public static void runHamJar() throws HamException {
        String commandLine = "java ";

        var externalJsonPath  =Path.of(getRootPath(),"ham","external.json").toString();
        commandLine+=" \"-Djsonconfig="+externalJsonPath+"\" ";
        var libsPath  =Path.of(getRootPath(),"ham","libs").toString();
        commandLine+=" \"-Dloader.path="+libsPath+"\" -Dloader.main=org.kendar.Main ";

        var appPathRootPath = Path.of(getRootPath(),"ham","app","target");
        File[] matchingFiles = appPathRootPath.toFile().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("app-") && name.endsWith(".jar");
            }
        });
        var appPathRoot  =Path.of(matchingFiles[0].getAbsolutePath()).toString();
        commandLine+=" -jar \""+appPathRoot+"\"";
        runJar(commandLine,
                "Start proxy server at port:1080",
                "Https server LOADED, port: 443",
                "Standard filters LOADED",
                "Dns server LOADED, port: 53",
                "Oidc server LOADED: www.local.test",
                "JsFilter server LOADED"
        );

    }

    public static boolean shutdownHookInitialized =false;
    public static void runJar(String command,String ... expected) throws HamException {

        System.out.println("STARTING "+command);
        initShutdownHook();
        try {
            if(processes.containsKey(command)){
                var process = processes.get(command);
                if(process!=null) {
                    if (process.process.isAlive()) {
                        return;
                    }
                    process.thread = null;
                }
                processes.remove(command);
            }
            processes.computeIfAbsent(command,(cm)->{
                try {
                    var ntpc = new ThreadAndProc();
                    ntpc.process = Runtime.getRuntime().exec(command);
                    ntpc.trace = new HashSet<String>(Arrays.asList(expected));
                    ntpc.thread = new Thread(new Runnable() {
                        public void run() {
                            BufferedReader input = new BufferedReader(new InputStreamReader(ntpc.process.getInputStream()));
                            String line;

                            try {
                                while ((line = input.readLine()) != null && ntpc.process.isAlive()) {
                                    var lineFixed = line;
                                    var founded = ntpc.trace.stream().filter(a->lineFixed.contains(a)).findFirst();
                                    if(founded.isPresent()){
                                        ntpc.trace.remove(founded.get());
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    ntpc.thread.start();
                    while(!ntpc.trace.isEmpty()){
                        Thread.sleep(1000);
                    }
                    return ntpc;
                }catch (Exception ex){
                    return null;
                }
            });

        }catch (Exception ex){
            throw new HamException("Unable to start "+command);
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
                    proc.process.destroy();



                }
            }

        });
    }
}

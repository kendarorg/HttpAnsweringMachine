package org.kendar.globaltest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class LogWriter {

    private static String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }
    public static void info(String data,Object ... pars){
        if(pars.length>0){
            System.out.println("[INFO] "+String.format(data,pars));
        }else{
            System.out.println("[INFO] "+data);
        }
    }
    public static void warn(String data,Object ... pars){
        if(pars.length>0){
            System.out.println("[WARN] "+String.format(data,pars));
        }else{
            System.out.println("[WARN] "+data);
        }
    }
    public static void errror(String data,Object ... pars){
        if(pars.length>0){
            System.err.println("[ERROR] "+String.format(data,pars));
        }else{
            System.err.println("[ERROR] "+data);
        }
    }
    private static final Path path;
    private static final Thread logWriter ;
    static {
        var startingPath = System.getenv("LOG_PATH");
        path = Path.of(startingPath,"globaltest."+(new Date().getTime())+".log");
        try {
            var logOnSystemOut = false;
            if(System.getenv("GLOBAL_LOG_ON_CONSOLE")!=null){
                logOnSystemOut = Boolean.parseBoolean(System.getenv("GLOBAL_LOG_ON_CONSOLE"));
            }
            if(!logOnSystemOut) {
                Files.writeString(path, "STARTING");
            }
        } catch (IOException e) {

        }
        logWriter = new Thread(LogWriter::writeLogs);
        logWriter.start();
    }

    private static final LinkedBlockingQueue<String> logs = new LinkedBlockingQueue<>();

    public static void writeProcess(String data){
        try {
            logs.put(data);
        } catch (InterruptedException e) {

        }
    }
    private static void writeLogs() {
        var logOnSystemOut = false;
        if(System.getenv("GLOBAL_LOG_ON_CONSOLE")!=null){
            logOnSystemOut = Boolean.parseBoolean(System.getenv("GLOBAL_LOG_ON_CONSOLE"));
        }

        while(true){
            try {
                while(!logs.isEmpty()){
                    var data = logs.poll();
                    if(data!=null){
                        data = data.trim()+"\n";
                    }
                    if(logOnSystemOut) {
                        System.out.println(data.trim());
                    }else {
                        if(!Files.exists(path)){
                            Files.writeString(path, "STARTING REMOVED!");
                        }
                        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                            if (data != null) {
                                writer.write(data);
                            }
                        } catch (IOException ioe) {
                            LogWriter.errror("IOException: %s", ioe);
                        }
                    }
                }
                Thread.sleep(1000);
            }catch (Exception ex){

            }
        }
    }

    public static void errror(Exception ex) {
        System.err.println("[ERROR] "+ex.getMessage());
        System.err.println(ex);
    }
}

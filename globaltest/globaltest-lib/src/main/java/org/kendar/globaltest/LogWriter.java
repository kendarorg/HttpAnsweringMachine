package org.kendar.globaltest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class LogWriter {

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

    private static void write(String level,String data,Object ... pars){

    }
    private static Path path;
    private static Thread logWriter ;
    static {
        path = Path.of("globaltest."+(new Date().getTime())+".log");
        try {
            Files.writeString(path,"STARTING");
        } catch (IOException e) {

        }
        logWriter = new Thread(LogWriter::writeLogs);
        logWriter.start();
    }

    private static LinkedBlockingQueue<String> logs = new LinkedBlockingQueue<>();

    public static void writeProcess(String data){
        try {
            logs.put(data);
        } catch (InterruptedException e) {

        }
    }
    private static void writeLogs() {
        while(true){
            try {
                while(!logs.isEmpty()){
                    var data = logs.poll();
                    if(data!=null){
                        data = data.trim()+"\n";
                    }
                    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                        writer.write(data);
                    } catch (IOException ioe) {
                        LogWriter.errror("IOException: %s", ioe);
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

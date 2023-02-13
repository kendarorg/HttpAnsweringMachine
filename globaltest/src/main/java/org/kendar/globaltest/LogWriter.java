package org.kendar.globaltest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class LogWriter {
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
                        System.err.format("IOException: %s", ioe);
                    }
                }
                Thread.sleep(1000);
            }catch (Exception ex){

            }
        }
    }
}

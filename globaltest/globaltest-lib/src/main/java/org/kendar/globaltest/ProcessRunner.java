package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ProcessRunner {
    private String command;
    private List<String> parameters = new ArrayList<>();
    private String startingPath;
    private BiConsumer<String,Process> errorConsumer;
    private BiConsumer<String,Process> outConsumer;
    private ConcurrentLinkedQueue<String> queue;
    private Map<String, String> env = new HashMap<>();


    public ProcessRunner(){}
    public ProcessRunner(Map<String,String> env){
        this();
        this.env=env;
    }

    public ProcessRunner asShell(){
        if (SystemUtils.IS_OS_WINDOWS) {
            withCommand("cmd").withParameter("/C");
        }else{
            withCommand("bash").withParameter("-c");
        }
        return this;
    }
    public ProcessRunner withCommand(String command) {
        this.command = command;
        return this;
    }

    public ProcessRunner wihEnvironment(Map<String,String> env) {
        this.env = env;
        return this;
    }



    public ProcessRunner withNoOutput(){
        errorConsumer=(a,p)->{};
        outConsumer=(a,p)->{};
        return this;
    }
    public ProcessRunner withParameter(String parameter){
        if(!SystemUtils.IS_OS_WINDOWS) {
            this.parameters.add(parameter);
        }else{
            this.parameters.add("\""+parameter+"\"");
        }
        return this;
    }

    public ProcessRunner withParameters(List<String> parameter){
        for(var p:parameter){
            withParameter(p);
        }
        return this;
    }

    public ProcessRunner withParameterPlain(String parameter){
        this.parameters.add(parameter);
        return this;
    }
    public ProcessRunner withStartingPath(String startingPath){
        this.startingPath = startingPath;
        return this;
    }

    public ProcessRunner withErr(BiConsumer<String,Process> errorConsumer){
        this.errorConsumer = errorConsumer;
        return this;
    }

    public ProcessRunner withOut(BiConsumer<String,Process> outConsumer){
        this.outConsumer = outConsumer;
        return this;
    }

    public ProcessRunner withStorage(ConcurrentLinkedQueue<String> queue){
        this.queue = queue;
        this.outConsumer = (a,p)->{queue.add(a);};
        this.errorConsumer = (a,p)->{queue.add(a);};
        return this;
    }

    public ProcessRunner run() throws Exception {
        return run(false);
    }

    public ProcessRunner runBackground() throws Exception {
        return run(true);

    }

    private ProcessRunner run(boolean background) throws Exception {
        var realCommand = new ArrayList<String>();
        realCommand.add(command);
        realCommand.addAll(parameters);
        var processBuilder = new ProcessBuilder(realCommand);
        for(var kvp:env.entrySet()){
            processBuilder.environment().put(kvp.getKey(),kvp.getValue());
        }
        if(startingPath!=null) {
            processBuilder.directory(new File(startingPath));
        }
        if(errorConsumer==null){
            errorConsumer = (a,p)->{System.err.println(a);};
        }
        if(outConsumer==null){
            outConsumer = (a,p)->{System.out.println(a);};
        }
        var process = processBuilder.start();


        var rc = String.join(" ",realCommand);
        if(startingPath!=null){
            if(!rc.startsWith(startingPath)){
                rc = startingPath+File.separatorChar+rc;
            }
        }
        LogWriter.writeProcess("[INFO] Running "+rc);
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
                (a)->{
                    LogWriter.writeProcess(a);
            outConsumer.accept(a,process);
                });
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),
                (a)->{
                    LogWriter.writeProcess(a);
                        errorConsumer.accept(a,process);
    });

        new Thread(outputGobbler).start();
        new Thread(errorGobbler).start();
        if(!background) {
            process.waitFor();
        }
        return this;
    }

    public ProcessRunner runSimple() throws Exception {
        var realCommand = new ArrayList<String>();
        realCommand.add(command);
        realCommand.addAll(parameters);
        var processBuilder = new ProcessBuilder(String.join(" ",realCommand));
        for(var kvp:env.entrySet()){
            processBuilder.environment().put(kvp.getKey(),kvp.getValue());
        }
        if(startingPath!=null) {
            processBuilder.directory(new File(startingPath));
        }
        if(errorConsumer==null){
            errorConsumer = (a,p)->System.err.println(a);
        }
        if(outConsumer==null){
            outConsumer = (a,p)->System.out.println(a);
        }

        var process = processBuilder.start();


        var rc = String.join(" ",realCommand);
        if(startingPath!=null){
            if(!rc.startsWith(startingPath)){
                rc = startingPath+File.separatorChar+rc;
            }
        }
        LogWriter.writeProcess("[INFO] Running "+rc);
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
                (a)->{LogWriter.writeProcess(a);outConsumer.accept(a,process);});
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),
                (a)->{LogWriter.writeProcess(a);errorConsumer.accept(a,process);});

        new Thread(outputGobbler).start();
        new Thread(errorGobbler).start();
        process.waitFor();
        return this;
    }

}

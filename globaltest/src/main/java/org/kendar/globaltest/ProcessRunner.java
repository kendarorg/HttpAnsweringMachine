package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
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
        this.outConsumer = (a,p)->this.queue.add(a);
        this.errorConsumer = (a,p)->this.queue.add(a);
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
            errorConsumer = (a,p)->System.err.println(a);
        }
        if(outConsumer==null){
            outConsumer = (a,p)->System.out.println(a);
        }

        var process = processBuilder.start();


        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
                (a)->outConsumer.accept(a,process));
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),
                (a)->errorConsumer.accept(a,process));

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


        //System.out.println(process.info().commandLine());
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
                (a)->outConsumer.accept(a,process));
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),
                (a)->errorConsumer.accept(a,process));

        new Thread(outputGobbler).start();
        new Thread(errorGobbler).start();
        process.waitFor();
        return this;
    }

}

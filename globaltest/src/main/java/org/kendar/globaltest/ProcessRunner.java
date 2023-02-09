package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ProcessRunner {
    private String command;
    private List<String> parameters = new ArrayList<>();
    private String startingPath;
    private Consumer<String> errorConsumer;
    private Consumer<String> outConsumer;
    private ConcurrentLinkedQueue<String> queue;

    public ProcessRunner(){}
    public ProcessRunner withCommand(String command) {
        this.command = command;
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
    public ProcessRunner withStartingPath(String startingPath){
        this.startingPath = startingPath;
        return this;
    }

    public ProcessRunner withErr(Consumer<String> errorConsumer){
        this.errorConsumer = errorConsumer;
        return this;
    }

    public ProcessRunner withOut(Consumer<String> outConsumer){
        this.outConsumer = outConsumer;
        return this;
    }

    public ProcessRunner withStorage(ConcurrentLinkedQueue<String> queue){
        this.queue = queue;
        this.outConsumer = (a)->this.queue.add(a);
        this.errorConsumer = (a)->this.queue.add(a);
        return this;
    }


    public ProcessRunner run() throws Exception {
        var realCommand = new ArrayList<String>();
        realCommand.add(command);
        realCommand.addAll(parameters);
        var processBuilder = new ProcessBuilder(realCommand);
        if(startingPath!=null) {
            processBuilder.directory(new File(startingPath));
        }
        if(errorConsumer==null){
            errorConsumer = (a)->System.err.println(a);
        }
        if(outConsumer==null){
            outConsumer = (a)->System.out.println(a);
        }
        var process = processBuilder.start();
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), outConsumer);
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), errorConsumer);

        new Thread(outputGobbler).start();
        new Thread(errorGobbler).start();
        process.waitFor();
        return this;
    }

}

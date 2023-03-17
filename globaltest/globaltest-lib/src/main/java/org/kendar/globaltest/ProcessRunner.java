package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class ProcessRunner {
    private String command;
    private final List<String> parameters = new ArrayList<>();
    private String startingPath;
    private BiConsumer<String, Process> errorConsumer;
    private BiConsumer<String, Process> outConsumer;
    private ConcurrentLinkedQueue<String> queue;
    private Map<String, String> env = new HashMap<>();
    private boolean isShell;
    private int maxOut = Integer.MAX_VALUE;


    public ProcessRunner() {
    }

    public ProcessRunner(Map<String, String> env) {
        this();
        this.env = env;
    }

    public ProcessRunner asShell() {
        this.isShell = true;
        return this;
    }

    public ProcessRunner withCommand(String command) {
        this.command = command;
        return this;
    }

    public ProcessRunner wihEnvironment(Map<String, String> env) {
        this.env = env;
        return this;
    }


    public ProcessRunner withNoOutput() {
        errorConsumer = (a, p) -> {
        };
        outConsumer = (a, p) -> {
        };
        return this;
    }

    public ProcessRunner withParameter(String parameter) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            this.parameters.add(parameter);
        } else {
            this.parameters.add("\"" + parameter + "\"");
        }
        return this;
    }

    public ProcessRunner withParameters(List<String> parameter) {
        for (var p : parameter) {
            withParameter(p);
        }
        return this;
    }

    public ProcessRunner withParameterPlain(String parameter) {
        this.parameters.add(parameter);
        return this;
    }

    public ProcessRunner withStartingPath(String startingPath) {
        this.startingPath = startingPath;
        return this;
    }

    public ProcessRunner withErr(BiConsumer<String, Process> errorConsumer) {
        this.errorConsumer = errorConsumer;
        return this;
    }

    public ProcessRunner withOut(BiConsumer<String, Process> outConsumer) {
        this.outConsumer = outConsumer;
        return this;
    }

    public ProcessRunner withStorage(ConcurrentLinkedQueue<String> queue) {
        this.queue = queue;
        this.outConsumer = (a, p) -> {
            queue.add(a);
        };
        this.errorConsumer = (a, p) -> {
            queue.add(a);
        };
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
        if(this.isShell){
            if(SystemUtils.IS_OS_WINDOWS){
                realCommand.add("CMD");
                realCommand.add("/C");
            }else{
                if(command.toLowerCase(Locale.getDefault()).endsWith(".sh")){
                    if(command.startsWith("/")){
                        command = "." + command;
                    }else {
                        command = "./" + command;
                    }
                    if(Files.exists(Path.of(command))){
                        chooseIfBashOrNot(realCommand,Path.of(command));
                    }else if(startingPath!=null && Files.exists(Path.of(startingPath,command))){
                        chooseIfBashOrNot(realCommand,Path.of(startingPath,command));
                    }else{
                        throw new Exception("Missing "+command);
                    }
                    realCommand.add(command);
                    realCommand.addAll(parameters);
                }else{
                    realCommand.add("bash");
                    realCommand.add("-c");
                    var fullCommand = command+" "+String.join(" ",parameters);
                    realCommand.add(fullCommand);
                    //realCommand.addAll(parameters);
                }

            }
        }else{
            realCommand.add(command);
            realCommand.addAll(parameters);
        }
        var processBuilder = new ProcessBuilder(realCommand);
        for (var kvp : env.entrySet()) {
            processBuilder.environment().put(kvp.getKey(), kvp.getValue());
        }
        if (startingPath != null) {
            processBuilder.directory(new File(startingPath));
        }
        if (errorConsumer == null) {
            errorConsumer = (a, p) -> {
                System.err.println(a);
            };
        }
        if (outConsumer == null) {
            outConsumer = (a, p) -> {
                System.out.println(a);
            };
        }
        var process = processBuilder.start();


        var rc = String.join(" ", realCommand);
        if (startingPath != null) {
            if (!rc.startsWith(startingPath)) {
                rc = startingPath + File.separatorChar + rc;
            }
        }

        maxLines.set(maxOut);
        LogWriter.writeProcess("[INFO] Running " + rc);
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
                (a) -> {
                    outConsumer.accept(a, process);
                    if (maxLines.get() <= 0) return;
                    maxLines.decrementAndGet();
                    LogWriter.writeProcess(a);
                });
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),
                (a) -> {
                    LogWriter.writeProcess(a);
                    errorConsumer.accept(a, process);
                });

        new Thread(outputGobbler).start();
        new Thread(errorGobbler).start();
        if (!background) {
            process.waitFor();
        }
        return this;
    }

    private void chooseIfBashOrNot(ArrayList<String> realCommand,Path path) throws IOException {
        var txt = Files.readString(path);
        var bashIndex = txt.indexOf("#!/bin/bash");
        var shIndex = txt.indexOf("#!/bin/sh");
        var isBash=true;
        if(shIndex>=0){
            if(bashIndex<0 || bashIndex>shIndex){
                isBash=false;
            }
        }
        if(isBash){
            realCommand.add("bash");
            realCommand.add("-c");
        }else{
            realCommand.add("sh");
            realCommand.add("-c");
        }
    }

    private AtomicInteger maxLines = new AtomicInteger(Integer.MAX_VALUE);


    public ProcessRunner limitOutput(int maxOut) {
        this.maxOut = maxOut;
        return this;
    }
}

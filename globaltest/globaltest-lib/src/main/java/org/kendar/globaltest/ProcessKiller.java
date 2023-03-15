package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProcessKiller {
    private Map<String, String> env;

    public ProcessKiller(Map<String,String> env){

        this.env = env;
    }
    public void killProcesses(Function<String,Boolean> check) throws Exception {
        killProcesses(false,check);
    }

    public void sigtermProcesses(Function<String,Boolean> check) throws Exception {
        killProcesses(true,check);
    }

    private void killProcesses(boolean sigTerm,Function<String,Boolean> check) throws Exception {
        if(!SystemUtils.IS_OS_WINDOWS) {
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("ps").
                    withParameter("-ef").
                    withStorage(queue).
                    run();
            var allJavaProcesses = queue.stream().
                    filter(a->check.apply(a.toLowerCase(Locale.ROOT))).
                    collect(Collectors.toList());
            for(var javaHam:allJavaProcesses){
                var spl = javaHam.trim().split("\\s+");
                var pid = spl[1];
                var message = sigTerm?"-15":"-9";
                new ProcessRunner(env).
                        withCommand("kill").
                        withParameter(message).
                        withParameter(pid).
                        withNoOutput().
                        run();
            }
        }else{
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("wmic").
                    withParameter("process").
                    withParameter("list").
                    withParameterPlain("/format:csv").
                    withStorage(queue).
                    run();
            var allJavaProcesses = queue.stream().
                    filter(a->check.apply(a.toLowerCase(Locale.ROOT))).
                    collect(Collectors.toList());
            for(var javaHam:allJavaProcesses){
                //var str="XPS15-KENDAR,,XPS15-KENDAR,System Idle Process,,,0,0,,20510591875000,,,System Idle Process,Microsoft Windows 11 Pro|C:\\WINDOWS|\\Device\\Harddisk0\\Partition3,0,0,9,60,0,60,8192,8,0,61440,0,1,0,1,0,0,0,0,,,32,0,8192,10.0.22621,8192,0,0";

                var spl = javaHam.trim().split(",");
                var pid = spl[spl.length-17];
                var pr =new ProcessRunner(env).
                        withCommand("taskkill");
                //if(!sigTerm) {
                pr.withParameter("/f");
                //}
                pr.withParameter("/pid").
                        withParameter(pid).
                        withNoOutput().
                        run();

            }
        }
    }
}

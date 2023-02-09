package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Main {
    public void test() throws Exception {
        var queue = new ConcurrentLinkedQueue<String>();
        new ProcessRunner().
                withCommand("ps").
                withParameter("-ef").
                withStorage(queue).
                run();
        for(var item:queue){
            System.out.println(item);
        }
    }
    private static void killAllHamProcesses() throws Exception {
        if(!SystemUtils.IS_OS_WINDOWS) {
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner().
                    withCommand("ps").
                    withParameter("-ef").
                    withStorage(queue).
                    run();
            var allJavaProcesses = queue.stream().filter(a->{
                return a.toLowerCase(Locale.ROOT).contains("java") &&
                        a.toLowerCase(Locale.ROOT).contains("httpanswering")
                        && !a.toLowerCase(Locale.ROOT).contains("globaltest")
                        ;
            }).collect(Collectors.toList());
            for(var javaHam:allJavaProcesses){
                var spl = javaHam.trim().split("\\s+");
                var pid = spl[1];
                new ProcessRunner().
                        withCommand("kill").
                        withParameter("-9").
                        withParameter(pid).
                        withNoOutput().
                        run();
            }
        }else{
            throw new RuntimeException("NOT IMPLEMENTED");
        }
    }
    public static void main(String[] args) throws Exception {
        var startingPath = System.getenv("STARTING_PATH");
        killAllHamProcesses();

    }


}

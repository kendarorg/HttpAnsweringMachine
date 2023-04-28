package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessUtils {
    private final Map<String, String> env;

    public ProcessUtils(Map<String, String> env) {

        this.env = env;
    }

    public void killProcesses(Function<String, Boolean> check) throws Exception {
        killProcesses(false, check);
    }

    public void sigtermProcesses(Function<String, Boolean> check) throws Exception {
        killProcesses(true, check);
    }

    private void killProcesses(boolean sigTerm, Function<String, Boolean> check, String... others) throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("ps").
                    withParameter("-ef").
                    withStorage(queue).
                    limitOutput(5).
                    run();
            var allJavaProcesses = queue.stream().
                    filter(a -> {
                        if (check.apply(a.toLowerCase(Locale.ROOT))) return true;
                        return false;
                    }).
                    collect(Collectors.toList());
            for (var javaHam : allJavaProcesses) {
                var spl = javaHam.trim().split("\\s+");
                var pid = spl[1];
                var message = sigTerm ? "-15" : "-9";
                new ProcessRunner(env).
                        withCommand("kill").
                        withParameter(message).
                        withParameter(pid).
                        withNoOutput().
                        run();
            }
        } else {
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("wmic").
                    withParameter("process").
                    withParameter("list").
                    withParameterPlain("/format:csv").
                    withStorage(queue).
                    limitOutput(5).
                    run();
            var allJavaProcesses = queue.stream().
                    filter(a -> check.apply(a.toLowerCase(Locale.ROOT))).
                    collect(Collectors.toList());
            for (var javaHam : allJavaProcesses) {
                //var str="XPS15-KENDAR,,XPS15-KENDAR,System Idle Process,,,0,0,,20510591875000,,,System Idle Process,Microsoft Windows 11 Pro|C:\\WINDOWS|\\Device\\Harddisk0\\Partition3,0,0,9,60,0,60,8192,8,0,61440,0,1,0,1,0,0,0,0,,,32,0,8192,10.0.22621,8192,0,0";

                var spl = javaHam.trim().split(",");
                var res = spl.length - 17;
                if (res <= 0 || javaHam.startsWith("Node,")) {
                    continue;
                }
                var pid = spl[res];
                var pr = new ProcessRunner(env).
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


    public void chmodExec(String dir, String... exts) throws Exception {
        Path of = Path.of(dir);
        if (!Files.exists(of)) {
            Files.createDirectories(of);
        }

        if (SystemUtils.IS_OS_WINDOWS) return;
        /*LocalFileUtils.runOnEveryFile(dir, Arrays.stream(exts).collect(Collectors.toList()), (p)->{
            new File(p).setExecutable(true);
        });*/
        for (var ext : exts) {
            new ProcessRunner(env).
                    asShell().
                    withCommand("chmod +x *." + ext + " ").withStartingPath(dir).withNoOutput().run();
        }
    }
}

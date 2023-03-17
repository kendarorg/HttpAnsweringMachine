package org.kendar.globaltest;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class LocalFileUtils {

    public static String execScriptExt() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return ".bat";
        }
        return ".sh";
    }


    public static String pathOf(String first, String... pars) {
        return Path.of(first, pars).toString();
    }
    public static void dos2unix(String directoryName, String ... exts) {
        runOnEveryFile(directoryName, Arrays.stream(exts).collect(Collectors.toList()),LocalFileUtils::dos2unix);
    }
    public static void runOnEveryFile(String directoryName, List<String> exts, Consumer<String> handlePath) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        if(fList==null) return;
        for (File file : fList) {
            if (file.isFile()) {
                var lowerName = file.getName().toLowerCase(Locale.ROOT);
                if(exts.stream().anyMatch(e->lowerName.endsWith("."+e))) {
                    handlePath.accept(file.getAbsolutePath());
                }
            } else if (file.isDirectory()) {
                if (file.getName().startsWith(".")) {
                    continue;
                }
                runOnEveryFile(file.getAbsolutePath(),exts,handlePath);
            }
        }
    }

    private static void dos2unix(String absolutePath) {
        try {
            Path of = Path.of(absolutePath);
            var content = Files.readString(of);
            if (content.indexOf("\r\n") > 0) {
                var result = content.replace("\r\n", "\n");
                Files.writeString(of, result);
            }
        } catch (IOException e) {

        }
    }
}

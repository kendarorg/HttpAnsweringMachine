package org.kendar.globaltest;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TarUtils {
    public static void extract(String startingPath, String name, Runnable onError) throws Exception {
        File initialFile = new File(startingPath + File.separatorChar + name + ".tar.gz");
        var in = new FileInputStream(initialFile);
        var BUFFER_SIZE = 64000;
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                /* If the entry is a directory, create the directory. */
                if (entry.isDirectory()) {
                    var entryName = entry.getName();
                    if (entryName.equalsIgnoreCase("./")) continue;
                    entryName = startingPath + entryName.substring(1);
                    File f = new File(entryName);
                    boolean created = f.mkdir();
                    if (!created) {
                        LogWriter.errror("Unable to create directory '%s', during extraction of archive contents.", f.getAbsolutePath());
                        onError.run();
                    }
                } else {
                    int count;
                    byte data[] = new byte[BUFFER_SIZE];
                    var entryName = entry.getName();
                    entryName = startingPath + entryName.substring(1);
                    var fos = new FileOutputStream(entryName, false);
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                    }
                }
            }

            LogWriter.info("Untar completed successfully!");
        }
    }
}

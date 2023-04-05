package org.kendar.utils;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class FullDownloadUploadService {
    private final List<FullDownloadUpload> downloadUploadList;
    private final Logger logger;

    public FullDownloadUploadService(List<FullDownloadUpload> downloadUploadList, LoggerBuilder builder) {

        this.downloadUploadList = downloadUploadList;
        this.logger= builder.build(FullDownloadUploadService.class);
    }

    public byte[] retrieveItems() throws Exception {
        logger.info("Downloading full settings");
        //ZipFile war = new ZipFile("war.zip");
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream("append.zip"));
        append.setLevel(Deflater.BEST_COMPRESSION);

        for (var du : downloadUploadList) {
            for (var su : du.retrieveItems().entrySet()) {
                var path = du.getId() + "/" + su.getKey();
                var content = su.getValue();
                ZipEntry e = new ZipEntry(path);
                logger.info("append: " + e.getName());
                append.putNextEntry(e);
                append.write(su.getValue());
                append.closeEntry();
            }
        }
        append.close();
        //war.close();
        return Files.readAllBytes(Path.of("append.zip"));
    }

    public void uploadItems(byte[] input) throws Exception {
        logger.info("Uploading full settings");
        var data = new HashMap<String, HashMap<String, byte[]>>();
        try (var zi = new ZipInputStream(new ByteArrayInputStream(input))) {
            byte[] b = new byte[8192];
            int len;
            ZipEntry zipEntry;
            while ((zipEntry = zi.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    var path = zipEntry.getName().toLowerCase(Locale.ROOT).split("/");
                    if (!data.containsKey(path[0])) data.put(path[0], new HashMap<>());

                    try (var out = new ByteArrayOutputStream()) {
                        while ((len = zi.read(b)) > 0) {
                            out.write(b, 0, len);
                        }
                        data.get(path[0]).put(path[1], out.toByteArray());
                    }
                }
            }
        } catch (IOException e) {
            throw new Exception(e);
        }
        for (var handler : downloadUploadList) {
            var subData = data.get(handler.getId().toLowerCase(Locale.ROOT));
            handler.uploadItems(subData);
        }
    }
}
package org.kendar.utils;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.*;

@Component
public class FullDownloadUploadService {
    private final List<FullDownloadUpload> downloadUploadList;

    public FullDownloadUploadService(List<FullDownloadUpload> downloadUploadList){

        this.downloadUploadList = downloadUploadList;
    }

    public byte[] retrieveItems() throws Exception {
        //ZipFile war = new ZipFile("war.zip");
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream("append.zip"));
        append.setLevel(Deflater.BEST_COMPRESSION);

        for(var du:downloadUploadList){
            for(var su:du.retrieveItems().entrySet()){
                var path = du.getId()+"/"+su.getKey();
                var content = su.getValue();
                ZipEntry e = new ZipEntry(path);
                System.out.println("append: " + e.getName());
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
        var data = new HashMap<String,HashMap<String,byte[]>>();
        try(var zi = new ZipInputStream(new ByteArrayInputStream(input))) {
            byte[] b = new byte[8192];
            int len;
            ZipEntry zipEntry;
            while ((zipEntry = zi.getNextEntry()) != null) {
                if(!zipEntry.isDirectory()){
                    var path = zipEntry.getName().toLowerCase(Locale.ROOT).split("/");
                    if(!data.containsKey(path[0]))data.put(path[0],new HashMap<>());

                    try(var out = new ByteArrayOutputStream()) {
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
        for(var handler:downloadUploadList){
            var subData = data.get(handler.getId().toLowerCase(Locale.ROOT));
            handler.uploadItems(subData);
        }
    }
}
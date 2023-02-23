package org.kendar.utils;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Component
public class FullDownloadUploadService {
    private final List<FullDownloadUpload> downloadUploadList;

    public FullDownloadUploadService(List<FullDownloadUpload> downloadUploadList){

        this.downloadUploadList = downloadUploadList;
    }

    public byte[] retrieveItems() throws Exception {
        ZipFile war = new ZipFile("war.zip");
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream("append.zip"));
        append.setLevel(Deflater.BEST_COMPRESSION);

        var items = new HashMap<String,byte[]>();
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
        war.close();
        return Files.readAllBytes(Path.of("append.zip"));
    }

    public void uploadItems(InputStream input){
        throw new NotImplementedException();
    }
}
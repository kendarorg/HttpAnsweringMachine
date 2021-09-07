package org.kendar.utils;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileResourcesUtilsImpl implements FileResourcesUtils {
    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    public InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    @Override
    public List<String> getFileFromResourceAsString(String fileName) {
        var result = new ArrayList<String>();
        var is = getFileFromResourceAsStream(fileName);
        try (InputStreamReader streamReader =
                     new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            boolean first = false;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
        The resource URL is not working in the JAR
        If we try to access a file that is inside a JAR,
        It throws NoSuchFileException (linux), InvalidPathException (Windows)

        Resource URL Sample: file:java-io.jar!/json/file1.json
     */
    public File getFileFromResource(String fileName) throws URISyntaxException {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());
            return new File(resource.toURI());
        }
    }

    public String buildPath(String ... paths){
        String returnValue = null;
        var result = paths[0];
        try {
            for (var i = 1; i < paths.length; i++) {
                paths[i] = paths[i].replace("/",File.separator);
                paths[i] = paths[i].replace("\\",File.separator);
            }
            while(result.endsWith("/") || result.endsWith("\\")){
                result = result.substring(0,result.length()-1);
            }
            for (var i = 1; i < paths.length; i++) {
                if (i > 0) {
                    var cur = paths[i];
                    while(cur.endsWith("/") || cur.endsWith("\\")){
                        cur = cur.substring(0,cur.length()-1);
                    }
                    while(cur.startsWith("/")|| cur.startsWith("\\")){
                        cur = cur.substring(1);
                    }
                    result += File.separator + cur;
                }
            }
            var fpResult = result.replace('\\','/');
            var fp = new URI(fpResult);
            if(!fp.isAbsolute()){
                while(result.startsWith("/")||result.startsWith("\\")){
                    result = result.substring(1);
                }
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();
                returnValue = s+File.separator+result;
            }else {
                returnValue = result;
            }
            return returnValue;
        }catch(Exception ex){
            return returnValue;
        }
    }
}

package org.kendar.utils;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@Component
public class FileResourcesUtilsImpl implements FileResourcesUtils {
    private final Logger logger;

    public FileResourcesUtilsImpl(LoggerBuilder loggerBuilder){
        logger = loggerBuilder.build(FileResourcesUtilsImpl.class);
    }
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
            /*for (var i = 1; i < paths.length; i++) {
                paths[i] = paths[i].replace("/",File.separator);
                paths[i] = paths[i].replace("\\",File.separator);
            }
            while(result.endsWith("/") || result.endsWith("\\")){
                result = result.substring(0,result.length()-1);
            }
            for (var i = 1; i < paths.length; i++) {
                var cur = paths[i];
                while(cur.endsWith("/") || cur.endsWith("\\")){
                    cur = cur.substring(0,cur.length()-1);
                }
                while(cur.startsWith("/")|| cur.startsWith("\\")){
                    cur = cur.substring(1);
                }
                result += File.separator + cur;
            }*/
            result = buildPathRelative(paths);
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
            logger.error(ex.getMessage(),ex);
            return null;
        }
    }

    public String buildPathRelative(String ... paths){
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
                var cur = paths[i];
                while(cur.endsWith("/") || cur.endsWith("\\")){
                    cur = cur.substring(0,cur.length()-1);
                }
                while(cur.startsWith("/")|| cur.startsWith("\\")){
                    cur = cur.substring(1);
                }
                result += File.separator + cur;
            }

            return result;
        }catch(Exception ex){
            logger.error(ex.getMessage(),ex);
            return null;
        }
    }

    @Override
    public HashMap<String, Object> loadResources(Object clazz, String path) throws URISyntaxException, IOException {
        if(!path.startsWith("/")){
            path = "/"+path;
        }
        var result = new HashMap<String,Object>();
        URI uri = clazz.getClass().getResource(path).toURI();
        var classLoader = clazz.getClass().getClassLoader();

        Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            myPath = fileSystem.getPath(path);
        } else {
            myPath = Paths.get(uri);
        }
        Stream<Path> walk = Files.walk(myPath, 10);
        for (Iterator<Path> it = walk.iterator(); it.hasNext();){
            var foundedPath = it.next().toString();
            if(foundedPath.startsWith("/")){
                foundedPath=foundedPath.substring(1);
            }
            result.put(foundedPath, null);
        }
        for (var item :result.entrySet()){
            try {
                InputStream inputStream = classLoader.getResourceAsStream(item.getKey());
                try (inputStream) {
                    var bytes = inputStream.readAllBytes();
                    if(bytes.length>0) {
                        item.setValue(bytes);
                    }
                }
            }catch (Exception e){
                continue;
            }
        }
        return result;
    }
}

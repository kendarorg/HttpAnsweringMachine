package org.kendar.utils;

import org.slf4j.Logger;
import org.springframework.boot.loader.Launcher;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class FileResourcesUtilsImpl implements FileResourcesUtils {
  private final Logger logger;

  public FileResourcesUtilsImpl(LoggerBuilder loggerBuilder) {
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
  public byte[] getFileFromResourceAsByteArray(String fileName) throws IOException {
    var initialStream = getFileFromResourceAsStream(fileName);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[1024];
    while ((nRead = initialStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    byte[] byteArray = buffer.toByteArray();
    buffer.close();
    return byteArray;
  }

  @Override
  public List<String> getFileFromResourceAsString(String fileName) {
    var result = new ArrayList<String>();
    var is = getFileFromResourceAsStream(fileName);
    try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
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
      // return new File(resource.getFile());
      return new File(resource.toURI());
    }
  }

  public String buildPath(String... paths) {
    String returnValue;
    var result = paths[0];
    try {
      result = buildPathRelative(paths);
      var fpResult = result.replace('\\', '/');
      var fp = new URI(fpResult);
      if (!fp.isAbsolute()) {
        while (result.startsWith("/") || result.startsWith("\\")) {
          result = result.substring(1);
        }
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        returnValue = s + File.separator + result;
      } else {
        returnValue = result;
      }
      return returnValue;
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      return null;
    }
  }

  public String buildPathRelative(String... paths) {
    StringBuilder result = new StringBuilder(paths[0]);
    try {
      for (var i = 1; i < paths.length; i++) {
        paths[i] = paths[i].replace("/", File.separator);
        paths[i] = paths[i].replace("\\", File.separator);
      }
      while (result.toString().endsWith("/") || result.toString().endsWith("\\")) {
        result = new StringBuilder(result.substring(0, result.length() - 1));
      }
      for (var i = 1; i < paths.length; i++) {
        var cur = paths[i];
        while (cur.endsWith("/") || cur.endsWith("\\")) {
          cur = cur.substring(0, cur.length() - 1);
        }
        while (cur.startsWith("/") || cur.startsWith("\\")) {
          cur = cur.substring(1);
        }
        result.append(File.separator).append(cur);
      }

      return result.toString();
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      return null;
    }
  }

  @Override
  public HashMap<String, Object> loadResources(Object clazz, String path) throws IOException {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    var result = new HashMap<String, Object>();

    var classLoader = clazz.getClass().getClassLoader();

    final File jarFile =
        new File(clazz.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

    var ress = new ArrayList<String>();
    if (jarFile.isFile()) { // Run with JAR file
      loadFromJar(path, result, classLoader, jarFile, ress,null);
    } else if(isCanonical(jarFile)) {// Run with IDE
      loadFromClasspathInIDE(path, result, jarFile);
    }else {
      loadNestedJar(path, result, classLoader, jarFile, ress);
    }

    return result;
  }

  /**
   * Handles file:\C:\Private\HttpAnsweringMachine\ham\app-1.0-SNAPSHOT.jar!\BOOT-INF\classes!
   * @param path  /C:/Private/HttpAnsweringMachine/ham/libs/docker.builder-1.0-SNAPSHOT.jar
   * @param result
   * @param classLoader
   * @param jarFile
   * @param ress
   */
  private void loadNestedJar(String path, HashMap<String, Object> result, ClassLoader classLoader, File jarFile, ArrayList<String> ress) throws IOException {
    var splitted = jarFile.getPath().split("!");
    var rootFilePath = splitted[0].substring(5);
    var rootFile = new File(rootFilePath);
    logger.error("Loading nested jar  with path "+jarFile);
    loadFromJar(path, result, classLoader, rootFile, ress,"BOOT-INF/classes");
  }

  private boolean isCanonical(File jarFile) {
    try {
      jarFile.getCanonicalPath();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private void loadFromClasspathInIDE(String path, HashMap<String, Object> result, File jarFile) {
    final URL url = Launcher.class.getResource("/" + path);
    if (url != null) {
      try {
        final File apps = new File(url.toURI());
        loadDirFromJar(path, result, jarFile, apps);
      } catch (URISyntaxException ex) {
        // never happens
      }
    }
  }

  private void loadFromJar(
      String path,
      HashMap<String, Object> result,
      ClassLoader classLoader,
      File jarFile,
      ArrayList<String> ress,String extra)
      throws IOException {
    if(extra!=null){
      path = extra+"/"+path;
    }
    final JarFile jar = new JarFile(jarFile);
    final Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
    while (entries.hasMoreElements()) {
      var nextElement = entries.nextElement();
      final String name = nextElement.getName();
      if (nextElement.isDirectory()) continue;
      if (name.startsWith(path + "/")) { // filter according to the path
        ress.add(name);
      }
    }
    jar.close();
    for (var filePath : ress) {
      try {
        InputStream inputStream = classLoader.getResourceAsStream(filePath);
        if(inputStream==null){
          continue;
        }
        try (inputStream) {
          var bytes = inputStream.readAllBytes();
          if (bytes.length > 0) {
            //web/test.css
            //BOOT-INF/classes/web/test.css
            if(extra!=null){
              filePath = filePath.substring(extra.length());
            }
            if (filePath.startsWith("/")) filePath = filePath.substring(1);
            result.put(filePath, bytes);
          }
        }
      } catch (Exception e) {
        logger.trace(e.getMessage());
      }
    }
  }

  private void loadDirFromJar(
      String path, HashMap<String, Object> result, File jarFile, File apps) {
    if(apps ==null ) return;
    var listFiles = apps.listFiles();
    if(listFiles==null) return;
    for (File app : listFiles) {
      if (app.isDirectory()) {
        loadDirFromJar(path, result, jarFile, app);
      } else {
        loadSingleFileFromJar(path, result, jarFile, app);
      }
    }
  }

  private void loadSingleFileFromJar(
      String path, HashMap<String, Object> result, File jarFile, File app) {
    var filePath = app.getPath();
    try {
      var bytes = Files.readAllBytes(Paths.get(app.getPath()));
      if (bytes.length > 0) {
        var internalpath = filePath.replace(jarFile.getPath(), "").replace("\\", "/");
        if (internalpath.startsWith("/")) {
          internalpath = internalpath.substring(1);
        }

        result.put(internalpath, bytes);
      }
    } catch (Exception ex) {
      logger.trace(ex.getMessage());
    }
  }
}

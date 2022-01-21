package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.http.CustomFiltersLoader;
import org.kendar.http.FilterDescriptor;
import org.kendar.servers.JsonConfiguration;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class JsFilterLoader implements CustomFiltersLoader {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final SandboxClassShutter sandboxClassShutter = new SandboxClassShutter();
  private final JsonConfiguration configuration;
  private final String jsFilterPath;
  private final EventQueue eventQueue;
  private final ExternalRequester externalRequester;
  private final Environment environment;
  private final Logger logger;
  private final LoggerBuilder loggerBuilder;
  private final FileResourcesUtils fileResourcesUtils;
  private ScriptableObject globalScope;

  public JsFilterLoader(
          Environment environment,
          LoggerBuilder loggerBuilder,
          FileResourcesUtils fileResourcesUtils,
          JsonConfiguration configuration,
          EventQueue eventQueue,
          ExternalRequester externalRequester) {

    this.environment = environment;
    this.logger = loggerBuilder.build(JsFilterLoader.class);
    this.loggerBuilder = loggerBuilder;
    this.fileResourcesUtils = fileResourcesUtils;
    jsFilterPath = configuration.getConfiguration(JsFilterConfig.class).getPath();
    this.eventQueue = eventQueue;
    this.externalRequester = externalRequester;
    logger.info("JsFilter LOADED");
    this.configuration = configuration;
  }

  public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }

  protected Scriptable getNewScope(Context cx) {
    // global scope lazy initialization
    if (globalScope == null) {
      globalScope = cx.initStandardObjects();
    }

    return cx.newObject(globalScope);
  }

  public List<FilterDescriptor> loadFilters() {
    var result = new ArrayList<FilterDescriptor>();
    String currentPath = "";
    // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
    try {
      File f;
      var realPath = fileResourcesUtils.buildPath(jsFilterPath);
      f = new File(realPath);
      if (f.exists()) {
        var pathnames = f.list();
        if (pathnames != null) {
          // For each pathname in the pathnames array
          for (String pathname : pathnames) {
            var fullPath = fileResourcesUtils.buildPath(jsFilterPath, pathname);
            currentPath = fullPath;
            loadSinglePlugin(result, realPath, fullPath);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error compiling js filter " + currentPath, e);
    }
    return result;
  }

  private void loadSinglePlugin(
      ArrayList<FilterDescriptor> result, String realPath, String fullPath) throws Exception {
    var newFile = new File(fullPath);
    if (newFile.isFile() && fullPath.toLowerCase(Locale.ROOT).endsWith(".json")) {
      var data = Files.readString(Path.of(fullPath));
      var filterDescriptor = mapper.readValue(data, JsFilterDescriptor.class);

      filterDescriptor.setRoot(realPath+File.separator+filterDescriptor.getId());
      precompileFilter(filterDescriptor);
      var executor =
          new JsFilterExecutor(filterDescriptor, this, loggerBuilder, filterDescriptor.getId());
      var fd = new FilterDescriptor(this, executor, environment, configuration);
      result.add(fd);
    }
  }

  @Override
  public FilterDescriptor loadFilterFile(String fileName, byte[] fileData, boolean overwrite) {
    try {
      String jsonDescriptor = null;
      var fullStringPath = fileResourcesUtils.buildPath(jsFilterPath);
      var realPath = new File(fullStringPath);
      byte[] buffer = new byte[1024];
      var input = new ByteArrayInputStream(fileData);
      ZipInputStream zis = new ZipInputStream(input);
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        File newFile = newFile(realPath, zipEntry);
        var path = Path.of(zipEntry.toString());
        if (path.getParent() == null
            && !zipEntry.isDirectory()
            && zipEntry.toString().toLowerCase().endsWith(".json")) {
          jsonDescriptor = zipEntry.toString();
        }
        if (zipEntry.isDirectory()) {
          if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
          }
        } else {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
          }

          // write file content
          if (newFile.exists() && !overwrite) {
            throw new IOException("Already existing!!");
          }
          FileOutputStream fos = new FileOutputStream(newFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();

      if (jsonDescriptor != null) {
        var fullPath = fileResourcesUtils.buildPath(jsFilterPath, jsonDescriptor);
        var result = new ArrayList<FilterDescriptor>();
        var localRealPath = fileResourcesUtils.buildPath(jsFilterPath);
        loadSinglePlugin(result, localRealPath, fullPath);
        return result.get(0);
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private void precompileFilter(JsFilterDescriptor filterDescriptor) throws Exception {
    StringBuilder scriptSrc =
        new StringBuilder(
            "var globalFilterResult=runFilter(JSON.parse(REQUESTJSON),JSON.parse(RESPONSEJSON),utils);\n"
                + "globalResult.put('request', JSON.stringify(globalFilterResult.request));\n"
                + "globalResult.put('response', JSON.stringify(globalFilterResult.response));\n"
                + "globalResult.put('continue', globalFilterResult.continue);\n");
    // Load all scripts
    for (var file : filterDescriptor.getRequires()) {
      scriptSrc
          .append("\r\n")
          .append(Files.readString(Path.of(filterDescriptor.getRoot() + File.separator + file)));
    }
    scriptSrc.append("\r\nfunction runFilter(request,response,utils){");
    for (var sourceLine :
            filterDescriptor.getSource()) {
      scriptSrc
              .append("\r\n")
              .append(sourceLine);
    }
    scriptSrc.append("\r\n}");

    Context cx = Context.enter();
    cx.setOptimizationLevel(9);
    cx.setLanguageVersion(Context.VERSION_1_8);
    //cx.setClassShutter(sandboxClassShutter);
    var realPath = fileResourcesUtils.buildPath(jsFilterPath);
    filterDescriptor.initializeQueue(new JsUtils(eventQueue,realPath,externalRequester));
    try {
      Scriptable currentScope = getNewScope(cx);
      filterDescriptor.setScript(cx.compileString(scriptSrc.toString(), "my_script_id", 1, null));
    } catch (Exception ex) {
      logger.error("Error compiling script");
      logger.error(scriptSrc.toString());
      throw new Exception(ex);
    } finally {
      Context.exit();
    }
  }

  public static class SandboxClassShutter implements ClassShutter {
    public boolean visibleToScripts(String fullClassName) {
      return fullClassName.equals(HashMap.class.getName());
    }
  }
}

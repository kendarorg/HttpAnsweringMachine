package org.kendar.http;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.MimeChecker;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StaticWebFilter implements FilteringClass {
  private final FileResourcesUtils fileResourcesUtils;
  final Parser parser = Parser.builder().build();
  private HashMap<String, Object> resourceFiles = new HashMap<>();
  private final ConcurrentHashMap<String, String> markdownCache = new ConcurrentHashMap<>();

  public StaticWebFilter(FileResourcesUtils fileResourcesUtils) {
    this.fileResourcesUtils = fileResourcesUtils;
  }

  protected abstract String getPath();

  public String getDescription() {
    return null;
  }

  public String getAddress() {
    return null;
  }

  @PostConstruct
  public void loadAllStuffs() throws IOException {
    var realPath = getPath();
    if (isResource(getPath())) {
      realPath = realPath.substring(1);
      resourceFiles = fileResourcesUtils.loadResources(this, realPath);
    }
  }

  @SuppressWarnings("RedundantIfStatement")
  @HttpMethodFilter(phase = HttpFilterType.STATIC, pathAddress = "*", method = "GET", id = "null")
  public boolean handle(Request request, Response response) {
    var realPath = getPath();
    if (isResource(getPath())) {
      realPath = realPath.substring(1);
    }

    if (verifyPathAndRender(response, realPath, request.getPath())) return true;
    if (verifyPathAndRender(response, realPath, request.getPath() + "/index.htm")) return true;
    if (verifyPathAndRender(response, realPath, request.getPath() + "/index.html")) return true;
    if (verifyPathAndRender(response, realPath, request.getPath() + ".md")) return true;
    if (verifyPathAndRender(response, realPath, request.getPath() + "/index.md")) return true;

    return false;
  }

  private boolean verifyPathAndRender(Response response, String realPath, String possibleMatch) {
    Path fullPath;
    if (resourceFiles == null || resourceFiles.isEmpty()) {
      fullPath = Path.of(fileResourcesUtils.buildPath(realPath, possibleMatch));
    } else {
      fullPath = Path.of(realPath, possibleMatch);
    }
    if (isFileExisting(fullPath)) {
      renderFile(fullPath, response);
      return true;
    }
    return false;
  }

  private boolean isFileExisting(Path fullPath) {
    if (fullPath == null) return false;
    var resourcePath = fullPath.toString().replace('\\', '/');
    if (resourceFiles == null || resourceFiles.isEmpty()) {
      return Files.exists(fullPath) && !Files.isDirectory(fullPath);
    } else if (resourceFiles.containsKey(resourcePath)) {
      var data = resourceFiles.get(resourcePath);
      if (data == null) return false;
      return ((byte[]) data).length > 0;
    } else {
      return false;
    }
  }

  private boolean isResource(String path) {
    return path.startsWith("*");
  }

  private void renderFile(Path fullPath, Response response) {
    try {
      var stringPath = fullPath.toString();
      String mimeType = null;
      if (resourceFiles == null || resourceFiles.isEmpty()) {
        mimeType = Files.probeContentType(fullPath);
      }
      if (mimeType == null) {
        if (stringPath.endsWith(".js")) {
          mimeType = "text/javascript";
        } else if (stringPath.endsWith(".css")) {
          mimeType = "text/css";
        } else if (stringPath.endsWith(".htm") || stringPath.endsWith(".html")) {
          mimeType = "text/html";
        } else if (stringPath.endsWith(".md")) {
          mimeType = "text/html";
        } else {
          mimeType = "application/octet-stream";
        }
      }
      response.setBinaryResponse(MimeChecker.isBinary(mimeType, null));
      if (resourceFiles == null || resourceFiles.isEmpty()) {
        renderRealFile(fullPath, response, stringPath);
      } else {
        renderResourceFile(response, stringPath);
      }
      response.addHeader("Content-Type", mimeType);
      response.setStatusCode(200);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void renderResourceFile(Response response, String stringPath) {
    var resourcePath = stringPath.replace('\\', '/');
    if (response.isBinaryResponse()) {
      response.setResponseBytes((byte[]) resourceFiles.get(resourcePath));
    } else if (!stringPath.endsWith(".md")) {
      response.setResponseText(new String((byte[]) resourceFiles.get(resourcePath)));
    } else if (stringPath.endsWith(".md")) {
      response.setResponseText(
          renderMarkdown(null, new String((byte[]) resourceFiles.get(resourcePath))));
    }
  }

  private void renderRealFile(Path fullPath, Response response, String stringPath) throws IOException {
    if (response.isBinaryResponse()) {
      response.setResponseBytes(Files.readAllBytes(fullPath));
    } else if (!stringPath.endsWith(".md")) {
      response.setResponseText(Files.readString(fullPath));
    } else if (stringPath.endsWith(".md")) {
      response.setResponseText(renderMarkdown(stringPath, Files.readString(fullPath)));
    }
  }

  private String renderMarkdown(String path, String readString) {
    if (path != null) {
      if (!markdownCache.containsKey(path)) {
        markdownCache.put(path, internalRender(readString));
      }
      return markdownCache.get(path);
    }
    return internalRender(readString);
  }

  private String internalRender(String readString) {
    Node document = parser.parse(readString);
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    return renderer.render(document);
  }
}

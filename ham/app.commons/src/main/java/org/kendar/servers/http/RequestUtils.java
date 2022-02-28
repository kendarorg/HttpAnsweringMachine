package org.kendar.servers.http;

import com.sun.net.httpserver.Headers;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class RequestUtils {
  public static boolean isMethodWithBody(Request result) {
    return result.getMethod().equalsIgnoreCase("POST")
        || result.getMethod().equalsIgnoreCase("PUT")
        || result.getMethod().equalsIgnoreCase("PATCH");
  }

  public static String getFromMap(Map<String, String> map, String index) {

    if (map.containsKey(index)) {
      return map.get(index);
    }
    for (var entry : map.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(index)) {
        return entry.getValue();
      }
    }
    return null;
  }

  public static void addToMap(Map<String, String> map, String key, String value) {

    for (var entry : map.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(key)) {
        map.put(entry.getKey(), value);
        return;
      }
    }
    map.put(key, value);
  }

  public static String removeFromMap(Map<String, String> map, String index) {
    if (map.containsKey(index)) {
      String data = map.get(index);
      map.remove(index);
      return data;
    }
    return null;
  }

  public static Map<String, String> queryToMap(String qs) {
    Map<String, String> result = new HashMap<>();
    if (qs == null) return result;

    int last = 0, next, l = qs.length();
    while (last < l) {
      next = qs.indexOf('&', last);
      if (next == -1) next = l;

      if (next > last) {
        int eqPos = qs.indexOf('=', last);
        if (eqPos < 0 || eqPos > next)
          result.put(URLDecoder.decode(qs.substring(last, next), UTF_8), "");
        else
          result.put(
              URLDecoder.decode(qs.substring(last, eqPos), UTF_8),
              URLDecoder.decode(qs.substring(eqPos + 1, next), UTF_8));
      }
      last = next + 1;
    }
    return result;
  }

  public static Map<String, String> headersToMap(Headers requestHeaders) {
    var result = new HashMap<String, String>();
    for (var entry : requestHeaders.entrySet()) {
      if (entry.getValue() == null || entry.getValue().size() == 0) {
        result.put(entry.getKey(), "");
      } else {
        result.put(entry.getKey(), entry.getValue().get(0));
      }
    }

    return result;
  }

  public static Map<String, String> parseContentDisposition(String value) {
    var result = new HashMap<String, String>();
    var cd = ContentDisposition.parse(value);

    result.put("charset", cd.getCharset());
    result.put("filename", cd.getFilename() == null ? "file" : cd.getFilename());
    result.put("name", cd.getName() == null ? "file" : cd.getName());
    result.put("type", cd.getType() == null ? "application/octet-stream" : cd.getType());
    return result;
  }

  private static String byteListToString(List<Byte> l, Charset charset) {
    if (l == null) {
      return "";
    }
    byte[] array = new byte[l.size()];
    int i = 0;
    for (Byte current : l) {
      array[i] = current;
      i++;
    }
    return new String(array, charset);
  }

  public static String sanitizePath(Request result) {
    return result.getHost() + result.getPath();
  }

  public static List<MultipartPart> buildMultipart(byte[] body, String boundary, String contentType)
      throws FileUploadException {
    Charset encoding = UTF_8;
    RequestContext requestContext = new SimpleRequestContext(encoding, contentType, body);
    FileUploadBase fileUploadBase = new PortletFileUpload();
    FileItemFactory fileItemFactory = new DiskFileItemFactory();
    fileUploadBase.setFileItemFactory(fileItemFactory);
    fileUploadBase.setHeaderEncoding(encoding.displayName());
    List<FileItem> fileItems = fileUploadBase.parseRequest(requestContext);

    List<MultipartPart> result = new ArrayList<>();
    for (var fileItem : fileItems) {
      result.add(new MultipartPart(fileItem));
    }
    return result;
  }

  public static String buildFullAddress(Request request,boolean usePort) {

    String port = "";

    if(usePort) {
      if (request.getPort() != -1) {
        if (request.getPort() != 443 && request.getProtocol().equalsIgnoreCase("https")) {
          port = ":" + request.getPort();
        }

        if (request.getPort() != 80 && request.getProtocol().equalsIgnoreCase("http")) {
          port = ":" + request.getPort();
        }
      }
    }
    return request.getProtocol()
            + "://"
            + request.getHost()
            + port
            + request.getPath()
            + buildFullQuery(request);
  }
  public static String buildFullQuery(Request request) {
    if (request.getQuery().size() == 0) return "";
    return "?"
            + request.getQuery().entrySet().stream()
            .map(
                    e ->
                            e.getKey()
                                    + "="
                                    + java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)
                                    .replace(" ", "%20"))
            .collect(joining("&"));
  }
}

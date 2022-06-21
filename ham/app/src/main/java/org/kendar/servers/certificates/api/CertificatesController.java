package org.kendar.servers.certificates.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class CertificatesController implements FilteringClass {
  final ObjectMapper mapper = new ObjectMapper();
  private final FileResourcesUtils fileResourcesUtils;
  private final Logger logger;

  public CertificatesController(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder,
                                EventQueue eventQueue) {
    logger = loggerBuilder.build(CertificatesController.class);

    this.fileResourcesUtils = fileResourcesUtils;
  }

  @Override
  public String getId() {
    return "org.kendar.servers.certificates.api.CertificatesController";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/certificates",
      method = "GET",
      id = "1012a4b4-277d-11ec-9621-0242ac130002")
  @HamDoc(
          description = "Retrieve the list of certificates",
          responses = {@HamResponse(body = String[].class)},
          requests = {@HamRequest()})
  public void listAllCertificates(Request req, Response res)
      throws IOException {
    var resources = fileResourcesUtils.loadResources(this, "certificates");

    var result = new ArrayList<String>();
    for (var resource : resources.keySet()) {
      var path = Path.of(resource);
      var stringPath = path.getFileName().toString();
      try {
        var byteContent =
            fileResourcesUtils.getFileFromResourceAsByteArray("certificates/" + stringPath);
        if (byteContent.length == 0) continue; // avoid directories
        result.add(stringPath);
      } catch (Exception ex) {
        logger.trace(ex.getMessage());
      }
    }
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(result));
  }
  /*
  application/pkcs8                   .p8  .key
  application/pkcs10                  .p10 .csr
  application/pkix-cert               .cer
  application/pkix-crl                .crl
  application/pkcs7-mime              .p7c

  application/x-x509-ca-cert          .crt .der
  application/x-x509-user-cert        .crt
  application/x-pkcs7-crl             .crl

  application/x-pem-file              .pem
  application/x-pkcs12                .p12 .pfx

  application/x-pkcs7-certificates    .p7b .spc
  application/x-pkcs7-certreqresp     .p7r
   */

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/certificates/{file}",
      method = "GET",
      id = "1014a4b4-277d-11ec-9621-0242ac130002")
  @HamDoc(
          description = "Retrieve the certificate",
          responses = {@HamResponse(
                  code = 200,
                  description = "Certificate",
                  body = String.class,
                  content = "text/plain"),
                  @HamResponse(
                          code = 200,
                          body = byte[].class,
                          description = "Zip with certificate",
                          content = "application/zip")},
          path = {@PathParameter(key="file")},
          query = {@QueryString(key="clear", description = "If set returns a plain text certificate")},
          requests = {@HamRequest()})
  public void getSingleCertificate(Request req, Response res)
      throws IOException {
    var resources = fileResourcesUtils.loadResources(this, "certificates");
    var name = req.getPathParameter("file");
    var returnClear = req.getQuery("clear")!=null;

    for (var resource : resources.keySet()) {
      var path = Path.of(resource).getFileName().toString();
      if (path.equalsIgnoreCase(name)) {
        var result = fileResourcesUtils.getFileFromResourceAsByteArray("certificates/" + path);

        if(!returnClear) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ZipOutputStream zos = new ZipOutputStream(baos);
          ZipEntry entry = new ZipEntry(path);
          entry.setSize(result.length);
          zos.putNextEntry(entry);
          zos.write(result);
          zos.closeEntry();
          zos.close();
          res.addHeader("Content-type", "application/zip");
          res.addHeader("Content-disposition", "inline;filename=" + path + ".zip");
          res.setResponseBytes(baos.toByteArray());
          res.setBinaryResponse(true);
        }else{
          res.setResponseBytes(result);
          res.setBinaryResponse(true);
          res.addHeader("Content-type", "application/octect-stream");
        }

        res.setBinaryResponse(true);
        return;
      }
    }
    res.setStatusCode(404);
    res.setResponseText("Unable to find " + name);
  }
}

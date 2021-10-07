package org.kendar.servers.certificates.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class CertificatesController implements FilteringClass {
    private FileResourcesUtils fileResourcesUtils;
    ObjectMapper mapper = new ObjectMapper();

    @Value("${localhost.name}")
    private String localhost;

    public CertificatesController(FileResourcesUtils fileResourcesUtils){

        this.fileResourcesUtils = fileResourcesUtils;
    }
    @Override
    public String getId() {
        return "org.kendar.servers.certificates.api.CertificatesController";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/certificates",
            method = "GET")
    public boolean listAllCertificates(Request req, Response res) throws IOException, URISyntaxException {
        var resources = fileResourcesUtils.loadResources(this,"certificates");

        var result = new ArrayList<String>();
        for(var resource : resources.keySet()){
            var path = Path.of(resource);
            var stringPath = path.getFileName().toString();
            var byteContent = fileResourcesUtils.getFileFromResourceAsByteArray("certificates/"+stringPath);
            if(byteContent.length==0)continue; //avoid directories
            result.add(stringPath);
        }
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
        return false;
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

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/certificates/{file}",
            method = "GET")
    public boolean getSingleCertificate(Request req, Response res) throws IOException, URISyntaxException {
        var resources = fileResourcesUtils.loadResources(this,"certificates");
        var name = req.getPathParameter("file");

        for(var resource : resources.keySet()){
            var path = Path.of(resource).getFileName().toString();
            if(path.equalsIgnoreCase(name)){
                var result = fileResourcesUtils.getFileFromResourceAsByteArray("certificates/"+path);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos);
                ZipEntry entry = new ZipEntry(path);
                entry.setSize(result.length);
                zos.putNextEntry(entry);
                zos.write(result);
                zos.closeEntry();
                zos.close();
                res.addHeader("Content-type", "application/zip");
                res.addHeader("Content-disposition", "inline;filename="+path+".zip");

                res.setResponseBytes(baos.toByteArray());
                res.setBinaryResponse(true);
                return false;
            }
        }
        res.setStatusCode(404);
        res.setResponseText("Unable to find "+name);
        return false;
    }
}

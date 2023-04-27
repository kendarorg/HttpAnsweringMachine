package org.kendar.servers.certificates.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.kendar.events.EventQueue;
import org.kendar.events.events.SSLChangedEvent;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.certificates.CertificatesManager;
import org.kendar.servers.certificates.GeneratedCert;
import org.kendar.servers.certificates.api.models.TLSSSLGenerator;
import org.kendar.servers.config.SSLConfig;
import org.kendar.servers.config.SSLDomain;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class SSLController implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final CertificatesManager certificatesManager;
    private final LoggerBuilder loggerBuilder;
    private final FileResourcesUtils fileResourcesUtils;

    public SSLController(JsonConfiguration configuration,
                         EventQueue eventQueue,
                         CertificatesManager certificatesManager,
                         LoggerBuilder loggerBuilder,
                         FileResourcesUtils fileResourcesUtils) {

        this.configuration = configuration;
        this.eventQueue = eventQueue;
        this.certificatesManager = certificatesManager;
        this.loggerBuilder = loggerBuilder;
        this.fileResourcesUtils = fileResourcesUtils;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.certificates.api.SSLController";
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/ssl",
            method = "GET")
    @HamDoc(
            tags = {"base/ssl"},
            description = "Retrieve the list of ssl registrations",
            responses = {@HamResponse(
                    description = "SSL Domains",
                    body = SSLDomain[].class
            )})
    public void getExtraServers(Request req, Response res) throws JsonProcessingException {
        var domains = configuration.getConfiguration(SSLConfig.class).getDomains();
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(domains));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/ssl/{id}",
            method = "DELETE")
    @HamDoc(
            tags = {"base/ssl"},
            description = "Delete ssl item",
            path = @PathParameter(key = "id"))
    public void removeDnsServer(Request req, Response res) {
        var cloned = configuration.getConfiguration(SSLConfig.class).copy();

        var name = req.getPathParameter("id");

        ArrayList<SSLDomain> newList = new ArrayList<>();
        for (var item : cloned.getDomains()) {
            if (item.getId().equalsIgnoreCase(name)) {
                continue;
            }
            newList.add(item);
        }
        cloned.setDomains(newList);
        configuration.setConfiguration(cloned);
        res.setStatusCode(200);
        eventQueue.handle(new SSLChangedEvent());
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/ssl",
            method = "POST")
    @HamDoc(
            tags = {"base/ssl"},
            description = "Add one/many certificate/s",
            requests = {@HamRequest(
                    body = SSLDomain[].class
            ), @HamRequest(
                    body = SSLDomain.class
            )})
    public void addDnsServer(Request req, Response res) throws Exception {
        var cloned = configuration.getConfiguration(SSLConfig.class).copy();

        List<SSLDomain> newList = new ArrayList<>();
        if (req.getRequestText().startsWith("[")) {
            newList = (List<SSLDomain>) mapper.readValue(req.getRequestText(), List.class).stream()
                    .map(a -> {
                        var newDomain = new SSLDomain();
                        newDomain.setId(UUID.randomUUID().toString());
                        newDomain.setAddress((String) a);
                        return newDomain;
                    })
                    .distinct()
                    .collect(Collectors.toList());

        } else {
            newList.add(mapper.readValue(req.getRequestText(), SSLDomain.class));
        }
        final var newList2 = newList;
        var notNew = cloned.getDomains().stream()
                .filter(a -> newList2.stream().noneMatch(m -> m.getAddress().equalsIgnoreCase(a.getAddress())))
                .collect(Collectors.toList());
        newList.addAll(notNew);
        cloned.setDomains(newList);
        configuration.setConfiguration(cloned);
        res.setStatusCode(200);
        eventQueue.handle(new SSLChangedEvent());
    }

    //TODO: Generate SSL Certificate https://gist.github.com/fntlnz/cf14feb5a46b2eda428e000157447309
    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/sslgen",
            method = "POST")
    @HamDoc(
            tags = {"base/ssl"},
            description = "Generate SSL certificate for website",
            requests = @HamRequest(
                    body = TLSSSLGenerator.class
            ),
            responses = @HamResponse(
                    body = String.class,
                    content = "application/pkix-cert",
                    examples = @Example(
                            description = "Example certificate",
                            example = "-----BEGIN CERTIFICATE-----\n" +
                                    "BASDASDFASE34523452SAFSDAFSD\n" +
                                    "-----END CERTIFICATE-----\n"
                    )
            )
    )
    public void generateSSL(Request req, Response res) throws Exception {

        var request = mapper.readValue(req.getRequestText(), TLSSSLGenerator.class);

        var root = certificatesManager.loadRootCertificate("certificates/ca.der", "certificates/ca.key");
        GeneratedCert domain =
                certificatesManager.createCertificate(
                        request.getCn(), null, root, request.getExtraDomains(), false);
        var encodedBytes = domain.certificate.getEncoded();
        //final FileOutputStream os = new FileOutputStream("target/local.gamlor.info.cer");
        String result = "-----BEGIN CERTIFICATE-----\n";
        result += new String(Base64.encodeBase64(encodedBytes, true));
        result += "-----END CERTIFICATE-----\n";
        res.setResponseText(result);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.PKIX_CERT);
        res.setStatusCode(200);
    }
}

package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.models.JsonFileData;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPIContent implements FilteringClass {
    private final FileResourcesUtils fileResourcesUtils;
    private final LoggerBuilder loggerBuilder;
    private final Md5Tester md5Tester;
    private final HibernateSessionFactory sessionFactory;
    ObjectMapper mapper = new ObjectMapper();

    public ReplayerAPIContent(
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester,
            JsonConfiguration configuration,
            HibernateSessionFactory sessionFactory) {

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
        this.md5Tester = md5Tester;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getId() {
        return "org.kendar.replayer.apis.ReplayerAPIContent";
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}/{requestOrResponse}",
            method = "GET")
    @HamDoc(description = "Retrieve the content of a request/response line", tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"), @PathParameter(key = "line"), @PathParameter(key = "requestOrResponse",
                    example = "request"
            )},
            responses = {
                    @HamResponse(
                            description = "Text content",
                            body = String.class
                    ),
                    @HamResponse(
                            description = "Binary content",
                            body = byte[].class
                    )
            }
    )
    public void retrieveContent(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(getPathParameter(req, "id"));
        var line = Long.parseLong(getPathParameter(req, "line"));
        var requestOrResponse = getPathParameter(req, "requestOrResponse");

        try {
            sessionFactory.query(em -> {
                var row = (ReplayerRow) em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
                        " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);
                sendBackContent(res, line, requestOrResponse, row);
            });
        } catch (Exception e) {
            res.setStatusCode(404);
            res.setResponseText("Missing id " + recordingId + " with line " + line);
        }
    }

    private String getPathParameter(Request req, String id) {
        return req.getPathParameter(id);
    }

    private void sendBackContent(
            Response res, long line, String requestOrResponse, ReplayerRow singleLine) {

        var allTypes = MimeTypes.getDefaultMimeTypes();
        if ("request".equalsIgnoreCase(requestOrResponse)) {
            var contentType = singleLine.getRequest().getHeader(ConstantsHeader.CONTENT_TYPE);
            if (ConstantsMime.JSON_SMILE.equalsIgnoreCase(contentType)) {
                res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
            } else {
                res.addHeader(ConstantsHeader.CONTENT_TYPE, contentType);
            }
            res.setBinaryResponse(singleLine.getRequest().isBinaryRequest());
            if (singleLine.getRequest().isBinaryRequest()) {
                res.setResponseBytes(singleLine.getRequest().getRequestBytes());
            } else {
                res.setResponseText(singleLine.getRequest().getRequestText());
            }

            setResultContentType(res, line, allTypes, contentType);

        } else if ("response".equalsIgnoreCase(requestOrResponse)) {
            var contentType = singleLine.getResponse().getHeader(ConstantsHeader.CONTENT_TYPE);
            if (ConstantsMime.JSON_SMILE.equalsIgnoreCase(contentType)) {
                res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
            } else {
                res.addHeader(ConstantsHeader.CONTENT_TYPE, contentType);
            }
            res.setBinaryResponse(singleLine.getResponse().isBinaryResponse());
            if (singleLine.getResponse().isBinaryResponse()) {
                res.setResponseBytes(singleLine.getResponse().getResponseBytes());
            } else {
                res.setResponseText(singleLine.getResponse().getResponseText());
            }
            setResultContentType(res, line, allTypes, contentType);
        }
        if (res.getHeader(ConstantsHeader.CONTENT_TYPE) == null) {
            if (res.isBinaryResponse()) {
                res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.STREAM);
            } else {
                res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
            }
        } else {
            if (res.isBinaryResponse()) {
                res.addHeader("Content-Disposition", "attachment;request." + line + ".bin");
            } else {
                res.addHeader("Content-Disposition", "attachment;request." + line + ".txt");
            }
        }
    }

    private void setResultContentType(Response res, long line, MimeTypes allTypes, String contentType) {
        try {
            MimeType mimeType = allTypes.forName(contentType);
            String ext = mimeType.getExtension();
            res.addHeader("Content-Disposition", "attachment;request." + line + ext);
        } catch (MimeTypeException e) {
            res.addHeader("Content-Disposition", "attachment;request." + line + ".bin");
        }
    }

    private void deleted(
            Response res, Long line, String requestOrResponse, ReplayerRow singleLine) {

        if ("request".equalsIgnoreCase(requestOrResponse)) {
            singleLine.getRequest().setRequestBytes(null);
            singleLine.getRequest().setRequestText(null);
            singleLine.setRequestHash("0");
        } else if ("response".equalsIgnoreCase(requestOrResponse)) {
            singleLine.getResponse().setResponseText(null);
            singleLine.getResponse().setResponseBytes(null);
            singleLine.setResponseHash("0");
        }
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}/{requestOrResponse}",
            method = "DELETE")
    @HamDoc(description = "Remove the content of a line", tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"), @PathParameter(key = "line"), @PathParameter(key = "requestOrResponse",
                    example = "request"
            )}
    )
    public void deleteConent(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(getPathParameter(req, "id"));
        var line = Long.parseLong(getPathParameter(req, "line"));
        var requestOrResponse = getPathParameter(req, "requestOrResponse");

        try {
            sessionFactory.transactional(em -> {
                var row = (ReplayerRow) em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
                        " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);
                deleted(res, line, requestOrResponse, row);
                em.remove(row);
            });

        } catch (Exception e) {
            res.setStatusCode(404);
            res.setResponseText("Missing id " + recordingId + " with line " + line);
        }
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}/{requestOrResponse}",
            method = "POST")
    @HamDoc(description = "Sets the content of a line", tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"), @PathParameter(key = "line"), @PathParameter(key = "requestOrResponse",
                    example = "request"
            )},
            requests = @HamRequest(
                    body = JsonFileData.class
            )
    )
    public void modifyContent(Request req, Response res)
            throws Exception {

        var recordingId = Long.parseLong(getPathParameter(req, "id"));
        var line = Long.parseLong(getPathParameter(req, "line"));
        var requestOrResponse = getPathParameter(req, "requestOrResponse");
        var data = mapper.readValue(req.getRequestText(), JsonFileData.class);

        try {
            sessionFactory.transactional(em -> {
                var ci = (CallIndex) em.createQuery("SELECT e FROM CallIndex e WHERE" +
                        " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);
                var row = (ReplayerRow) em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
                        " e.recordingId=" + recordingId + " AND e.id=" + ci.getReference()).getResultList().get(0);
                updated(line, requestOrResponse, row, data);
                em.merge(row);
            });

        } catch (Exception e) {
            res.setStatusCode(404);
            res.setResponseText("Missing id " + recordingId + " with line " + line);
        }
    }

    private void updated(
            Long line,
            String requestOrResponse,
            ReplayerRow singleLine,
            JsonFileData data)
            throws NoSuchAlgorithmException {
        if ("request".equalsIgnoreCase(requestOrResponse)) {
            singleLine.setRequestHash(md5Tester.calculateMd5(data.readAsByte()));
            var req = singleLine.getRequest();
            if (!data.matchContentType("text/plain")) {
                req.setRequestBytes(data.readAsByte());
                req.setBinaryRequest(true);
            } else {
                req.setRequestText(data.readAsString());
                req.setBinaryRequest(false);
            }
            singleLine.setRequest(req);

        } else if ("response".equalsIgnoreCase(requestOrResponse)) {
            singleLine.setResponseHash(md5Tester.calculateMd5(data.readAsByte()));
            var res = singleLine.getResponse();
            if (!data.matchContentType("text/plain")) {
                res.setResponseBytes(data.readAsByte());
                res.setBinaryResponse(true);
            } else {
                res.setResponseText(data.readAsString());
                res.setBinaryResponse(false);
            }
            singleLine.setResponse(res);
        }
    }
}

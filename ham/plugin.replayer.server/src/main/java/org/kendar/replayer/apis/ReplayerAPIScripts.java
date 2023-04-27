package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.apis.models.Scripts;
import org.kendar.replayer.engine.ReplayerEngine;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPIScripts implements FilteringClass {

    private final FileResourcesUtils fileResourcesUtils;
    private final LoggerBuilder loggerBuilder;
    final ObjectMapper mapper = new ObjectMapper();
    private final Md5Tester md5Tester;
    private final List<ReplayerEngine> engineList;
    private final HibernateSessionFactory sessionFactory;

    public ReplayerAPIScripts(
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester,
            List<ReplayerEngine> engineList,
            HibernateSessionFactory sessionFactory) {

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
        this.md5Tester = md5Tester;
        this.engineList = engineList;
        this.sessionFactory = sessionFactory;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/extension",
            method = "GET")
    @HamDoc(description = "retrieves the extensions", tags = {"plugin/replayer"},
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void retrieveExtensions(Request req, Response res) throws Exception {
        var result = engineList.stream().map(e -> e.getId()).collect(Collectors.toList());
        res.setStatusCode(200);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "GET")
    @HamDoc(description = "retrieves the scripts associate with a recording line", tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"), @PathParameter(key = "line")},
            responses = @HamResponse(
                    body = String.class
            )
    )
    public void retrieveScript(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(req.getPathParameter("id"));
        var line = Long.parseLong(req.getPathParameter("line"));

        sessionFactory.query(em -> {
            var prevId = (Long) em.createQuery("SELECT COALESCE( MAX(e.id),-1) FROM CallIndex e WHERE" +
                    " e.recordingId=" + recordingId + " AND e.id<" + line).getResultList().get(0);

            var nexId = (Long) em.createQuery("SELECT COALESCE( MIN(e.id),-1) FROM CallIndex e WHERE" +
                    " e.recordingId=" + recordingId + " AND e.id>" + line).getResultList().get(0);

            var index = (CallIndex) em.createQuery("SELECT e FROM CallIndex e WHERE" +
                    " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);
            var row = (ReplayerRow) em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
                    " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);


            res.addHeader("X-NEXT", "" + nexId);
            res.addHeader("X-PREV", "" + prevId);
            var result = new Scripts();
            result.setId(index.getId());
            result.setHost(row.getRequest().getHost());
            result.setPath(row.getRequest().getPath());
            result.setMethod(row.getRequest().getMethod());
            result.setPre(index.getPreScript());
            result.setPost(index.getPostScript());


            res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
            res.setResponseText(mapper.writeValueAsString(result));
        });
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "DELETE")
    @HamDoc(description = "delete the scripts associate with a recording line", tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"), @PathParameter(key = "line")}
    )
    public void deleteScript(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(req.getPathParameter("id"));
        var line = Long.parseLong(req.getPathParameter("line"));

        sessionFactory.transactional(em -> {
            var index = (CallIndex) em.createQuery("SELECT e FROM CallIndex e WHERE" +
                    " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);
            index.setPostScript(null);
            index.setPreScript(null);
            em.merge(index);
        });
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "PUT")
    @HamDoc(description = "modify/insert the scripts associate with a recording line", tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"), @PathParameter(key = "line")},
            responses = @HamResponse(
                    body = String.class
            )
    )
    public void putScript(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(req.getPathParameter("id"));
        var lines = Arrays.stream(req.getPathParameter("line").split(","))
                .map(Long::parseLong).collect(Collectors.toList());


        var data = mapper.readValue(req.getRequestText(), Scripts.class);

        sessionFactory.transactional(em -> {

            for (var line : lines) {
                var index = (CallIndex) em.createQuery("SELECT e FROM CallIndex e WHERE" +
                        " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);

                var row = (ReplayerRow) em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
                        " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);

                if (data.getPre() == null || data.getPre().trim().isEmpty()) {
                    index.setPreScript(null);
                } else {
                    index.setPreScript(data.getPre().trim());
                }

                if (data.getPost() == null || data.getPost().trim().isEmpty()) {
                    index.setPostScript(null);
                } else {
                    index.setPostScript(data.getPost().trim());
                }
                row.getRequest().setHost(data.getHost());
                row.getRequest().setPath(data.getPath());
                em.merge(index);
                em.merge(row);
            }
        });
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}

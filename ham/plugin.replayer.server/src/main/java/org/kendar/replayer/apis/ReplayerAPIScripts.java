package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.Scripts;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPIScripts implements FilteringClass {

    private final FileResourcesUtils fileResourcesUtils;
    private final LoggerBuilder loggerBuilder;
    private final DataReorganizer dataReorganizer;
    final ObjectMapper mapper = new ObjectMapper();
    private final Md5Tester md5Tester;
    private HibernateSessionFactory sessionFactory;
    private final String replayerData;

    public ReplayerAPIScripts(
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            DataReorganizer dataReorganizer,
            Md5Tester md5Tester,
            JsonConfiguration configuration,
            HibernateSessionFactory sessionFactory) {

        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();
        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
        this.dataReorganizer = dataReorganizer;
        this.md5Tester = md5Tester;
        this.sessionFactory = sessionFactory;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "GET")
    @HamDoc(description = "retrieves the scripts associate with a recording line",tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key = "line")},
            responses = @HamResponse(
                    body = String.class
            )
    )
    public void retrieveScript(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(req.getPathParameter("id"));
        var line = Long.parseLong(req.getPathParameter("line"));

        sessionFactory.query(em->{
            var prevId = (Long)em.createQuery("SELECT COALESCE( MAX(e.id),-1) FROM CallIndex e WHERE" +
                    " e.recordingId="+recordingId+" AND e.id<"+line).getResultList().get(0);

            var nexId = (Long)em.createQuery("SELECT COALESCE( MIN(e.id),-1) FROM CallIndex e WHERE" +
                    " e.recordingId="+recordingId+" AND e.id>"+line).getResultList().get(0);

            var index = (CallIndex)em.createQuery("SELECT e FROM CallIndex e WHERE" +
                    " e.recordingId="+recordingId+" AND e.id="+line).getResultList().get(0);
            var row = (ReplayerRow)em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
                    " e.recordingId="+recordingId+" AND e.id="+line).getResultList().get(0);


            res.addHeader("X-NEXT", ""+nexId);
            res.addHeader("X-PREV", ""+prevId);
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

      /*  var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();
        var prev = -1L;
        var next = -1L;
        //var result = new Scripts();
        var allItems = new ArrayList<>(datasetContent.getDynamicRequests());
        allItems.addAll(new ArrayList<>(datasetContent.getStaticRequests()));
        datasetContent.getIndexes().sort(Comparator.comparingLong(CallIndex::getId));
        for(var i=0;i<datasetContent.getIndexes().size();i++){
            var singleLine = datasetContent.getIndexes().get(i);
            if (singleLine.getId() == Integer.parseInt(line)) {
                var possible = allItems.stream().filter(a->a.getId()==singleLine.getReference()).findFirst();
                if(possible.isEmpty()){
                    continue;
                }
                var ref= possible.get();
                result.setId(Long.toString(singleLine.getId()));
                result.setHost(ref.getRequest().getHost());
                result.setPath(ref.getRequest().getPath());
                result.setMethod(ref.getRequest().getMethod());
                if (i > 0) {
                    prev = datasetContent.getIndexes().get(i - 1).getId();
                }
                if (i < (datasetContent.getIndexes().size() - 1)) {
                    next = datasetContent.getIndexes().get(i + 1).getId();
                }
                break;
            }
        }
        res.addHeader("X-NEXT", ""+next);
        res.addHeader("X-PREV", ""+prev);
        if(datasetContent.getPreScript().containsKey(line)){
            result.setPre(datasetContent.getPreScript().get(line));
        }
        if(datasetContent.getPostScript().containsKey(line)){
            result.setPost(datasetContent.getPostScript().get(line));
        }*/
    }



    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "DELETE")
    @HamDoc(description = "delete the scripts associate with a recording line",tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key = "line")}
    )
    public void deleteScript(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(req.getPathParameter("id"));
        var line = Long.parseLong(req.getPathParameter("line"));

        sessionFactory.transactional(em-> {
            var index = (CallIndex)em.createQuery("SELECT e FROM CallIndex e WHERE" +
                    " e.recordingId="+recordingId+" AND e.id="+line).getResultList().get(0);
            index.setPostScript(null);
            index.setPreScript(null);
            em.persist(index);
        });

       /* var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();
        datasetContent.getPostScript().remove(line);
        datasetContent.getPreScript().remove(line);
        dataset.justSave(datasetContent);*/
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "PUT")
    @HamDoc(description = "modify/insert the scripts associate with a recording line",tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key = "line")},
            responses = @HamResponse(
                    body = String.class
            )
    )
    public void putScript(Request req, Response res) throws Exception {
        var recordingId = Long.parseLong(req.getPathParameter("id"));
        var lines = Arrays.stream(req.getPathParameter("line").split(","))
                .map(Long::parseLong).collect(Collectors.toList());


        var data = mapper.readValue(req.getRequestText(),Scripts.class);

        sessionFactory.transactional(em-> {

            for(var line:lines) {
                var index = (CallIndex) em.createQuery("SELECT e FROM CallIndex e WHERE" +
                        " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);

                var row = (ReplayerRow)em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
                        " e.recordingId="+recordingId+" AND e.id="+line).getResultList().get(0);

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
                em.persist(index);
            }
        });
//
//        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
//
//        var dataset =
//                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
//        dataset.load(id, rootPath.toString(),null);
//        var datasetContent = dataset.load();
//
//        for(var line:lines) {
//            if (data.getPre() == null || data.getPre().trim().isEmpty()) {
//                datasetContent.getPreScript().remove(line + "");
//            } else {
//                datasetContent.getPreScript().put(line + "", data.getPre().trim());
//            }
//
//            if (data.getPost() == null || data.getPost().trim().isEmpty()) {
//                datasetContent.getPostScript().remove(line + "");
//            } else {
//                datasetContent.getPostScript().put(line + "", data.getPost().trim());
//            }
//        }
//
//        if(lines.size()==1) {
//            var line = lines.get(0);
//            ReplayerRow foundedRow = null;
//            for (var dyr : datasetContent.getDynamicRequests()) {
//                if (dyr.getId() == line) {
//                    foundedRow = dyr;
//                    break;
//                }
//            }
//            for (var dyr : datasetContent.getStaticRequests()) {
//                if (dyr.getId() == line) {
//                    foundedRow = dyr;
//                    break;
//                }
//            }
//            if (foundedRow != null) {
//                foundedRow.getRequest().setHost(data.getHost());
//                foundedRow.getRequest().setPath(data.getPath());
//            }
//        }
//        dataset.justSave(datasetContent);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}

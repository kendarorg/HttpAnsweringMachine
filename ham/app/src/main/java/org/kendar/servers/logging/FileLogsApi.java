package org.kendar.servers.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.Header;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.logging.model.FileLogListItem;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class FileLogsApi implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;
    private final LoggerBuilder loggerBuilder;
    private HibernateSessionFactory sessionFactory;

    public FileLogsApi(JsonConfiguration configuration,
                       LoggerBuilder loggerBuilder,
                       FileResourcesUtils fileResourcesUtils,
                       HibernateSessionFactory sessionFactory) {

        this.configuration = configuration;
        this.loggerBuilder = loggerBuilder;
        this.sessionFactory = sessionFactory;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/files",
            method = "GET")
    @HamDoc(
            description = "List all log files",
            responses = @HamResponse(
                    body = FileLogListItem[].class
            ), tags = {"base/logs"})
    public void getLogFiles(Request req, Response res) throws Exception {
        var index = Long.parseLong(req.getQuery("index"));
        var pageSize = Long.parseLong(req.getQuery("pageSize"));
        ArrayList<FileLogListItem> result = getFileLogListItems(index, pageSize);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result.stream().sorted(Comparator.comparing(FileLogListItem::getTimestamp)).collect(Collectors.toList())));

    }

    private ArrayList<FileLogListItem> getFileLogListItems(long index, long pageSize) throws Exception {
        var result = new ArrayList<FileLogListItem>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.ROOT);
        var start = index * pageSize;
        sessionFactory.query(em -> {
            var query = em.createQuery("SELECT e FROM LoggingTable e ORDER BY e.id ASC");
            query.setFirstResult((int) start);
            query.setMaxResults((int) (pageSize * 2));
            List<LoggingTable> rs = query.getResultList();
            for (var srs : rs) {
                var newItem = new FileLogListItem();
                newItem.setId(srs.getId());
                newItem.setHost(srs.getHost());
                newItem.setPath(srs.getPath());
                newItem.setTimestamp(srs.getTimestamp().getTime());
                var date = new Date(newItem.getTimestamp());
                newItem.setTime(sdf.format(date));
                result.add(newItem);

            }
        });
        return result;
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/files/{id}",
            method = "GET")
    @HamDoc(
            description = "Retrieve single log file",
            path = @PathParameter(key = "id"),
            responses = @HamResponse(
                    body = String.class,
                    description = "Content of the log file",
                    headers = {
                            @Header(key = "X-NEXT", description = "Next file id if present"),
                            @Header(key = "X-PREV", description = "Previous file id if present")
                    }
            ), tags = {"base/logs"})
    public void getLogFile(Request req, Response res) throws Exception {
        Long id = Long.parseLong(req.getPathParameter("id"));
        var result = new HashMap<String, Object>();
        sessionFactory.query(em -> {


            var prevId = (Long) em.createQuery("SELECT COALESCE( MAX(e.id),-1) FROM LoggingTable e WHERE" +
                    " e.id<" + id).getResultList().get(0);

            var nexIt = (Long) em.createQuery("SELECT COALESCE( MIN(e.id),-1) FROM LoggingTable e WHERE" +
                    " e.id>" + id).getResultList().get(0);


            var query = em.createQuery("SELECT e FROM LoggingTable e WHERE e.id=:id");
            query.setParameter("id", id);
            LoggingTable rs = (LoggingTable) query.getResultList().get(0);
            result.put("common", rs);

            query = em.createQuery("SELECT e FROM LoggingDataTable e WHERE e.id=:id");
            query.setParameter("id", id);
            LoggingDataTable rsld = (LoggingDataTable) query.getResultList().get(0);
            var reqs = mapper.readValue(rsld.getRequest(), Request.class);
            var ress = mapper.readValue(rsld.getResponse(), Response.class);
            result.put("request", reqs);
            result.put("response", ress);
            if (reqs.bodyExists() && !reqs.isBinaryRequest()) {
                result.put("request_body", reqs.getRequestText());
            }
            if (ress.bodyExists() && !ress.isBinaryResponse()) {
                result.put("response_body", ress.getResponseText());
            }

            if (prevId >= 0) {
                res.addHeader("X-PAST", prevId.toString());
            }
            if (nexIt != null) {
                res.addHeader("X-NEXT", nexIt.toString());
            }
        });

        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/files",
            method = "DELETE")
    @HamDoc(
            description = "Clean all log files",
            responses = @HamResponse(
                    body = FileLogListItem[].class
            ), tags = {"base/logs"})
    public void cleanLogFiles(Request req, Response res) throws Exception {
        sessionFactory.transactional(em -> {
            em.createQuery("DELETE FROM LoggingTable").executeUpdate();
            em.createQuery("DELETE FROM LoggingDataTable ").executeUpdate();
        });
    }
}

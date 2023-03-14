package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.logging.LoggingDataTable;
import org.kendar.servers.logging.LoggingTable;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.Calendar;

@Component
@HttpTypeFilter(hostAddress = "*", priority = 1)
public class RequestResponseFileLogging implements FilteringClass {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Logger responseLogger;
    private final Logger requestLogger;
    private final Logger staticLogger;
    private final Logger dynamicLogger;
    private final FileResourcesUtils fileResourcesUtils;
    private final JsonConfiguration configuration;
    private final Logger logger;
    private HibernateSessionFactory sessionFactory;
    private final Logger internalLogger;
    private String localAddress;

    public RequestResponseFileLogging(
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            JsonConfiguration configuration,
            HibernateSessionFactory sessionFactory) {

        this.fileResourcesUtils = fileResourcesUtils;
        this.responseLogger = loggerBuilder.build(Response.class);
        this.requestLogger = loggerBuilder.build(Request.class);
        this.staticLogger = loggerBuilder.build(StaticRequest.class);
        this.dynamicLogger = loggerBuilder.build(DynamicReqest.class);
        this.internalLogger = loggerBuilder.build(InternalRequest.class);
        this.configuration = configuration;
        this.logger = loggerBuilder.build(RequestResponseFileLogging.class);
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.http.RequestResponseFileLogging";
    }

    @PostConstruct
    public void init() throws Exception {
        var config = configuration.getConfiguration(GlobalConfig.class);

        localAddress = config.getLocalAddress();

    }

    private boolean isDebugOrMore(Logger le) {
        return le.isDebugEnabled() || le.isTraceEnabled();
    }

    private boolean isInfoOrMore(Logger le) {
        return le.isInfoEnabled() || le.isDebugEnabled() || le.isTraceEnabled();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.POST_RENDER,
            pathAddress = "*",
            method = "*")
    public boolean doLog(Request serReq, Response serRes) {
        var staticEnabled = isDebugOrMore(staticLogger) && serReq.isStaticRequest();
        var dynamicEnabled = isDebugOrMore(dynamicLogger) && !serReq.isStaticRequest();
        var internalEnabled = isDebugOrMore(internalLogger)&&localAddress.equalsIgnoreCase(serReq.getHost());
        var requestEnabled = isInfoOrMore(requestLogger);
        var responseEnabled = isInfoOrMore(requestLogger);

        if(!staticEnabled && !dynamicEnabled &&
            !internalEnabled && !requestEnabled && !responseEnabled){
            return false;
        }




        if (!staticEnabled) return false;
        if (!dynamicEnabled) return false;

        if (!internalEnabled && localAddress.equalsIgnoreCase(serReq.getHost())) {
            return false;
        }
        var rt = serReq.getRequestText();
        var rb = serReq.getRequestBytes();
        var st = serRes.getResponseText();
        var sb = serRes.getResponseBytes();

        if (isDebugOrMore(requestLogger) && serReq.getRequestText() != null) {
            if(serReq.getRequestText().length() > 100 && requestLogger.isDebugEnabled()) {
                serReq.setRequestText(serReq.getRequestText().substring(0, 100));
            }
        }else if(requestLogger.isInfoEnabled()){
            serReq.setRequestText(null);
        }

        if (isDebugOrMore(responseLogger) && serRes.getResponseText() != null) {
            if(serRes.getResponseText().length() > 100 && responseLogger.isDebugEnabled()) {
                serRes.setResponseText(serRes.getResponseText().substring(0, 100));
            }
        }else if(responseLogger.isInfoEnabled()){
            serRes.setResponseText(null);
        }
        serReq.setRequestBytes(null);
        serRes.setResponseBytes(null);

        try {
            //FileWriter myWriter = new FileWriter(filePath);

            sessionFactory.transactional((em) -> {
                var lt = new LoggingTable();
                lt.setProtocol(serReq.getProtocol());
                lt.setPath(serReq.getPath());
                lt.setMethod(serReq.getMethod());
                lt.setHost(serReq.getHost());
                lt.setRequestBody(serReq.bodyExists());
                lt.setResponseBody(serRes.bodyExists());
                lt.setContentType(serReq.getHeader("content-type"));
                lt.setTimestamp(Timestamp.from(Calendar.getInstance().toInstant()));
                em.persist(lt);

                var ld = new LoggingDataTable();
                ld.setId(lt.getId());
                ld.setResponse(mapper.writeValueAsString(serRes));
                ld.setRequest(mapper.writeValueAsString(serReq));
                em.persist(ld);

            });

        } catch (Exception ex) {
            logger.trace(ex.getMessage());
        }
        serReq.setRequestBytes(rb);
        serReq.setRequestText(rt);
        serRes.setResponseText(st);
        serRes.setResponseBytes(sb);
        return false;
    }

    private String getOptionalExtension(String filePath) {
        String extension = null;
        int i = filePath.lastIndexOf('.');
        int p = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        if (i > p) {
            extension = filePath.substring(i + 1);
        }
        return extension;
    }

    private String cleanUp(String s) {
        StringBuilder result = new StringBuilder();
        for (var c : s.toCharArray()) {
            if (c == '.') c = '-';
            if (c == '\\') c = '-';
            if (c == '/') c = '-';
            if (c == '`') c = '-';
            if (c == ':') c = '-';
            result.append(c);
        }
        return result.toString();
    }
}

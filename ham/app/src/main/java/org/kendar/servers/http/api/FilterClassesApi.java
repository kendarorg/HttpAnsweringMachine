package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.*;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.api.model.FilterDto;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class FilterClassesApi implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();
    private final FilterConfig filtersConfiguration;
    private final ApplicationContext context;
    private final JsonConfiguration configuration;

    public FilterClassesApi(
            FilterConfig filtersConfiguration,
            ApplicationContext context,
            JsonConfiguration configuration) {

        this.filtersConfiguration = filtersConfiguration;
        this.context = context;
        this.configuration = configuration;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.http.api.FilterClassesApi";
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/phase",
            method = "GET")
    @HamDoc(
            tags = {"base/filters"},
            description = "List all the possible phases",
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void listPhases(Request req, Response res) throws JsonProcessingException {
        var result = new ArrayList<FilterType>();
        result.add(new FilterType(0, HttpFilterType.NONE));
        result.add(new FilterType(1, HttpFilterType.PRE_RENDER));
        result.add(new FilterType(2, HttpFilterType.API));
        result.add(new FilterType(3, HttpFilterType.STATIC));
        result.add(new FilterType(4, HttpFilterType.PRE_CALL));
        result.add(new FilterType(5, HttpFilterType.POST_CALL));
        result.add(new FilterType(6, HttpFilterType.POST_RENDER));
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/phase/{phase}",
            method = "GET")
    @HamDoc(tags = {"base/filters"},
            description = "List the filters by phase",
            path = @PathParameter(key = "phase", description = "The phase of the filter"),
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void getFiltersForPhase(Request req, Response res) throws JsonProcessingException {
        var stringPhase = req.getPathParameter("phase");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filtersConfiguration.get();
        var result = new HashSet<String>();
        var listOfItems = config.filters.get(phase);

        for (var i = 0; i < listOfItems.size(); i++) {
            var item = listOfItems.get(i);
            var clazz = item.getClassId();
            result.add(clazz);
        }

        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result.toArray()));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/class",
            method = "GET")
    @HamDoc(tags = {"base/filters"},
            description = "List all the classes for Java filters",
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void getFiltersForClass(Request req, Response res) throws JsonProcessingException {
        var config = filtersConfiguration.get();

        var result = new ArrayList<>(config.filtersByClass.keySet());

        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result.toArray()));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/class/{clazz}",
            method = "GET")
    @HamDoc(tags = {"base/filters"},
            path = @PathParameter(key = "clazz", description = "Simple class name"),
            description = "List all the filters implemented by given class",
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void getIdFiltersForClass(Request req, Response res) throws JsonProcessingException {
        var globalConfig = configuration.getConfiguration(GlobalConfig.class);
        var clazz = req.getPathParameter("clazz");
        var config = filtersConfiguration.get();
        ArrayList<FilterDto> result = new ArrayList<>();
        var listOfItems = config.filtersByClass.get(clazz);

        for (var item : listOfItems) {
            var enabled =
                    globalConfig.checkFilterEnabled(item.getId())
                            && globalConfig.checkFilterEnabled(item.getClassId());
            var desc = new FilterDto(enabled, item.getTypeFilter(), item.getMethodFilter(), clazz);
            result.add(desc);
        }

        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result.toArray()));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/phase/{phase}/{clazz}",
            method = "GET")
    @HamDoc(tags = {"base/filters"},
            path = {@PathParameter(key = "phase"),
                    @PathParameter(key = "clazz")},
            description = "List all the filters implemented by given class for phase",
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void getFiltersForPhaseClass(Request req, Response res) throws JsonProcessingException {
        var globalConfig = configuration.getConfiguration(GlobalConfig.class);
        var stringPhase = req.getPathParameter("phase");
        var clazz = req.getPathParameter("clazz");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filtersConfiguration.get();
        var result = new ArrayList<FilterDto>();
        var listOfItems = config.filters.get(phase);
        for (var i = 0; i < listOfItems.size(); i++) {
            var item = listOfItems.get(i);
            if (!item.getClassId().equalsIgnoreCase(clazz)) continue;
            var enabled =
                    globalConfig.checkFilterEnabled(item.getId())
                            && globalConfig.checkFilterEnabled(item.getClassId());
            var desc = new FilterDto(enabled, item.getTypeFilter(), item.getMethodFilter(), clazz);
            result.add(desc);
        }

        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/id/{id}",
            method = "GET")
    @HamDoc(tags = {"base/filters"},
            path = @PathParameter(key = "id"),
            description = "List all the filters by id",
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void getFilterId(Request req, Response res) throws JsonProcessingException {
        var globalConfig = configuration.getConfiguration(GlobalConfig.class);
        var id = req.getPathParameter("id");
        var config = filtersConfiguration.get();
        var item = config.filtersById.get(id);

        writeFilterData(res, globalConfig, item);
    }

    private void writeFilterData(Response res, GlobalConfig globalConfig, FilterDescriptor item) throws JsonProcessingException {
        var enabled =
                globalConfig.checkFilterEnabled(item.getId())
                        && globalConfig.checkFilterEnabled(item.getClassId());
        var result = new FilterDto(enabled, item.getTypeFilter(), item.getMethodFilter(), item.getClassId());
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/id/{id}",
            method = "DELETE")
    @HamDoc(tags = {"base/filters"},
            path = @PathParameter(key = "id"),
            description = "Delete filter by id"
    )
    public void disableById(Request req, Response res) {
        var globalConfig = configuration.getConfiguration(GlobalConfig.class).copy();
        var id = req.getPathParameter("id");
        var config = filtersConfiguration.get();
        var item = config.filtersById.get(id);
        var filters = globalConfig.getFilters();
        filters.put(item.getId(), false);
        globalConfig.setFilters(filters);
        configuration.setConfiguration(globalConfig);
        res.setResponseText("");
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/id/{id}/enable",
            method = "PUT")
    @HamDoc(tags = {"base/filters"},
            path = @PathParameter(key = "id"),
            description = "Enable filter"
    )
    public void enableById(Request req, Response res) {
        var globalConfig = configuration.getConfiguration(GlobalConfig.class).copy();
        var id = req.getPathParameter("id");
        var config = filtersConfiguration.get();
        var item = config.filtersById.get(id);
        var filters = globalConfig.getFilters();
        filters.put(item.getId(), true);
        globalConfig.setFilters(filters);
        configuration.setConfiguration(globalConfig);
        res.setResponseText("");
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/loaders",
            method = "GET")
    @HamDoc(tags = {"base/filters"},
            description = "List all filter loaders",
            responses = @HamResponse(
                    body = String[].class
            ))
    public void getFiltersLoaders(Request req, Response res) throws JsonProcessingException {
        var result =
                context.getBeansOfType(CustomFiltersLoader.class).values().stream()
                        .map(customFiltersLoader -> customFiltersLoader.getClass().getSimpleName())
                        .collect(Collectors.toList());
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/filters/loaders/{loader}",
            method = "GET")
    @HamDoc(tags = {"base/filters"},
            path = @PathParameter(key = "loader"),
            description = "List all filters by loader",
            responses = @HamResponse(
                    body = String[].class
            )
    )
    public void getFiltersLoadersFilters(Request req, Response res) throws JsonProcessingException {
        var globalConfig = configuration.getConfiguration(GlobalConfig.class);
        var config = filtersConfiguration.get();
        var loader = req.getPathParameter("loader");
        var result = new ArrayList<FilterDto>();
        for (var item : config.filtersById.values()) {
            if (item.getLoader().getClass().getSimpleName().equalsIgnoreCase(loader)) {
                var enabled =
                        globalConfig.checkFilterEnabled(item.getId())
                                && globalConfig.checkFilterEnabled(item.getClassId());
                result.add(new FilterDto(enabled, item.getTypeFilter(), item.getMethodFilter(), item.getClassId()));
            }
        }
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    private void uploadNewScript(Request req, Response res, boolean overwrite) throws Exception {

        var globalConfig = configuration.getConfiguration(GlobalConfig.class);
        var loader = req.getQuery("loader");
        var stringPhase = req.getPathParameter("phase");
        var id = req.getPathParameter("id");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));

        var requiredLoader =
                context.getBeansOfType(CustomFiltersLoader.class).values().stream()
                        .filter(
                                customFiltersLoader ->
                                        customFiltersLoader.getClass().getSimpleName().equalsIgnoreCase(loader))
                        .findFirst();
        var config = filtersConfiguration.get().copy();

        String fileName = null;
        byte[] fileData = null;
        for (var mp : req.getMultipartData()) {
            if (!mp.isFile()) continue;
            fileName = mp.getFileName();
            fileData = mp.getByteData();
        }
        FilterDescriptor item = null;
        if (requiredLoader.isPresent()) {
            item = requiredLoader.get().loadFilterFile(fileName, fileData, overwrite);
            if (!item.getId().equals(id)) {
                throw new Exception("Filter ids not matching for update " + fileName);
            }
        }
        if (item == null) {
            throw new Exception("Unable to add filter " + fileName);
        }
        if (config.filtersById.containsKey(item.getId()) && !overwrite) {
            throw new Exception("Duplicate filter");
        }

        config.filtersById.put(item.getId(), item);
        var overwritten = false;
        for (var i = 0; i < config.filters.get(phase).size(); i++) {
            var founded = config.filters.get(phase).get(i);
            if (founded.getId().equals(item.getId())) {
                config.filters.get(phase).set(i, item);
                overwritten = true;
                break;
            }
        }
        if (!overwritten) {
            config.filters.get(phase).add(item);
        }
        filtersConfiguration.set(config);

        writeFilterData(res, globalConfig, item);
    }

    public static class FilterType {
        private HttpFilterType type;
        private int index;

        public FilterType(int index, HttpFilterType type) {

            this.index = index;
            this.type = type;
        }

        public HttpFilterType getType() {
            return type;
        }

        public void setType(HttpFilterType type) {
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}

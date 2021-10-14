package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.CustomFiltersLoader;
import org.kendar.http.FilterDescriptor;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.api.model.FilterDto;
import org.kendar.servers.http.configurations.FilterConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class FilterClassesApi  implements FilteringClass {
    private FilterConfig filteringClassesHandler;
    private Environment environment;
    private ApplicationContext context;

    public FilterClassesApi(FilterConfig filtersConfiguration, Environment environment,
                            ApplicationContext context){

        this.filteringClassesHandler = filtersConfiguration;
        this.environment = environment;
        this.context = context;
    }


    ObjectMapper mapper = new ObjectMapper();
    public class FilterType{
        public FilterType(int index,HttpFilterType type){

            this.index = index;
            this.type = type;
        }
        private HttpFilterType type;
        private int index;

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

    @Override
    public String getId() {
        return "org.kendar.servers.http.api.FilterClassesApi";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters",
            method = "GET",id="e907a4b4-277d-11ec-9621-0242ac130002")
    public void listPhases(Request req, Response res) throws JsonProcessingException {
        var result = new ArrayList<FilterType>();
        result.add(new FilterType(0,HttpFilterType.NONE));
        result.add(new FilterType(1,HttpFilterType.PRE_RENDER));
        result.add(new FilterType(2,HttpFilterType.API));
        result.add(new FilterType(3,HttpFilterType.STATIC));
        result.add(new FilterType(4,HttpFilterType.PRE_CALL));
        result.add(new FilterType(5,HttpFilterType.POST_CALL));
        result.add(new FilterType(6,HttpFilterType.POST_RENDER));
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}",
            method = "GET",id="e907a4b4-277d-11ec-9621-0242ac130003")
    public void getFiltersForPhase(Request req, Response res) throws JsonProcessingException {
        var stringPhase = req.getPathParameter("phase");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filteringClassesHandler.get();
        var result = new ArrayList<FilterDto>();
        var listOfItems = config.filters.get(phase);
        for(var i=0;i<listOfItems.size();i++){
            var item = listOfItems.get(i);
            var desc = new FilterDto(item.isEnabled(),item.getTypeFilter(),item.getMethodFilter());
            result.add(desc);
        }

        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}/{id}",
            method = "GET",id="e907a4b4-277d-11ec-9621-0242ac130004")
    public void getFilterId(Request req, Response res) throws JsonProcessingException {
        var stringPhase = req.getPathParameter("phase");
        var id = req.getPathParameter("id");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filteringClassesHandler.get();
        FilterDto result = null;
        var listOfItems = config.filters.get(phase);
        for(var i=0;i<listOfItems.size();i++){
            var item = listOfItems.get(i);
            if(item.getId().equalsIgnoreCase(id)){
                result = new FilterDto(item.isEnabled(),item.getTypeFilter(),item.getMethodFilter());
                break;
            }
        }

        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}/{id}",
            method = "DELETE",id="e9rea4b4-277d-11ec-9621-0242ac130004")
    public void removeFilterById(Request req, Response res) throws JsonProcessingException {
        var stringPhase = req.getPathParameter("phase");
        var id = req.getPathParameter("id");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filteringClassesHandler.get().copy();
        FilterDto result = null;
        var listOfItems = config.filters.get(phase);
        for(var i=0;i<listOfItems.size();i++){
            var item = listOfItems.get(i);
            if(item.getId().equalsIgnoreCase(id)){
                listOfItems.remove(i);
                config.filtersById.remove(item.getId());
                break;
            }
        }
        filteringClassesHandler.set(config);

        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}/{id}/status",
            method = "PUT",id="e967a4b4-277d-11ec-9621-0242ac130004")
    public void enableFilterById(Request req, Response res) throws JsonProcessingException {
        var enabled = Boolean.valueOf(req.getQuery("enabled"));
        var stringPhase = req.getPathParameter("phase");
        var id = req.getPathParameter("id");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filteringClassesHandler.get();

        var listOfItems = config.filters.get(phase);
        for(var i=0;i<listOfItems.size();i++){
            var item = listOfItems.get(i);
            if(item.getId().equalsIgnoreCase(id)){
                item.setEnabled(enabled);
                break;
            }
        }



    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}/{id}/status",
            method = "GET",id="e967a4b4-277d-44ec-9621-0242ac130004")
    public void getStatusById(Request req, Response res) throws JsonProcessingException {
        var enabled = Boolean.valueOf(req.getQuery("enabled"));
        var stringPhase = req.getPathParameter("phase");
        var id = req.getPathParameter("id");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filteringClassesHandler.get();
        var result = "false";
        var listOfItems = config.filters.get(phase);
        for(var i=0;i<listOfItems.size();i++){
            var item = listOfItems.get(i);
            if(item.getId().equalsIgnoreCase(id)){
                result =item.isEnabled()?"true":"false";
                break;
            }
        }

        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }



    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filtersloaders",
            method = "GET",id="e967a4b4-277d-41ecr9621-0242ac130004")
    public void getFiltersLoaders(Request req, Response res) throws JsonProcessingException {
        var result= context.getBeansOfType(CustomFiltersLoader.class).
                values().stream().map(customFiltersLoader ->
                customFiltersLoader.getClass().getSimpleName()).collect(Collectors.toList());
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }


    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filtersloaders/{loader}",
            method = "GET",id="e967a4b4-277d-41ecr9621y0242ac130004")
    public void getFiltersLoadersFilters(Request req, Response res) throws JsonProcessingException {
        var config = filteringClassesHandler.get();
        var loader = req.getPathParameter("loader");
        var result = new ArrayList<FilterDto>();
        for(var item :config.filtersById.values()){
            if(item.getLoader().getClass().getSimpleName().equalsIgnoreCase(loader)) {
                result.add(new FilterDto(item.isEnabled(), item.getTypeFilter(), item.getMethodFilter()));
            }
        }
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}",
            method = "POST",id="e967a4b4-2xxxx-44ec-9621-0242ac130004")
    public void addFilter(Request req, Response res) throws Exception {
        var overwrite = false;
        uploadNewScript(req, res, overwrite);
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}/{id}",
            method = "PUT",id="e967a4b4-2xxxx-44ec-9621-02g2ac130004")
    public void updateFilter(Request req, Response res) throws Exception {
        var overwrite = true;
        uploadNewScript(req, res, overwrite);
    }

    private void uploadNewScript(Request req, Response res, boolean overwrite) throws Exception {
        var loader = req.getQuery("loader");
        var stringPhase = req.getPathParameter("phase");
        var id = req.getPathParameter("id");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));

        var requiredLoader= context.getBeansOfType(CustomFiltersLoader.class).
                values().stream().filter(customFiltersLoader ->
                customFiltersLoader.getClass().getSimpleName().equalsIgnoreCase(loader)).
                findFirst();
        var config = filteringClassesHandler.get().copy();

        String fileName = null;
        byte[] fileData = null;
        for (var mp : req.getMultipartData()) {
            //var contendDisposition = RequestUtils.parseContentDisposition(mp.getHeader("Content-Disposition"));
            if (!mp.isFile()) continue;
            fileName = mp.getFileName();
            fileData = mp.getByteData();
        }
        FilterDescriptor item = requiredLoader.get().loadFilterFile(fileName,fileData, overwrite);
        if(item.getId()!=id){
            throw new Exception("Filter ids not matching for update "+fileName);
        }
        if(item==null){
            throw new Exception("Unable to add filter "+fileName);
        }
        if(config.filtersById.containsKey(item.getId()) && !overwrite){
            throw new Exception("Duplicate filter");
        }

        config.filtersById.put(item.getId(),item);
        var overwritten = false;
        for(var i=0;i<config.filters.get(phase).size();i++){
            var founded = config.filters.get(phase).get(i);
            if(founded.getId()==item.getId()){
                config.filters.get(phase).set(i,item);
                overwritten =true;
                break;
            }
        }
        if(!overwritten) {
            config.filters.get(phase).add(item);
        }
        filteringClassesHandler.set(config);

        var result = new FilterDto(item.isEnabled(),item.getTypeFilter(),item.getMethodFilter());
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }
}

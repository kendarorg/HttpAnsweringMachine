package org.kendar.servers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.api.model.RestFilter;
import org.kendar.servers.http.api.model.RestFilterRequire;
import org.kendar.servers.http.storage.DbFilter;
import org.kendar.servers.http.storage.DbFilterRequire;
import org.kendar.utils.FullDownloadUpload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JsDownloadUpload implements FullDownloadUpload {
    private final JsonConfiguration configuration;
    private final HibernateSessionFactory sessionFactory;

    public JsDownloadUpload(JsonConfiguration configuration, HibernateSessionFactory sessionFactory){

        this.configuration = configuration;
        this.sessionFactory = sessionFactory;
    }
    TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<HashMap<String, String>>() {};
    ObjectMapper mapper = new ObjectMapper();
    @Override
    public Map<String, byte[]> retrieveItems() throws Exception {
        var result = new HashMap<String,byte[]>();

        var dbFilters = (List<DbFilter>)sessionFactory.queryResult(em->
                em.createQuery("SELECT e FROM DbFilter e").getResultList());

        for(var dbFilter:dbFilters) {

            var rf = new RestFilter();
            rf.setId(dbFilter.getId());
            rf.setPhase(dbFilter.getPhase());
            rf.setPriority(dbFilter.getPriority());
            rf.setBlocking(dbFilter.isBlocking());
            rf.setName(dbFilter.getName());
            rf.setSource(dbFilter.getSource());
            rf.setType(dbFilter.getType());
            rf.setMatchers(mapper.readValue(dbFilter.getMatcher(), typeRef));
            rf.setRequire(new ArrayList<>());
            List<DbFilterRequire> rq = sessionFactory.queryResult(em ->
                    em.createQuery("SELECT e.name,e.binary FROM DbFilterRequire e WHERE e.scriptId=" + dbFilter.getId() + " ORDER BY e.id ASC").getResultList());
            for (var rqf : rq) {
                var dbf = new RestFilterRequire();
                dbf.setBinary(rqf.isBinary());
                dbf.setName(rqf.getName());
                dbf.setContent(rqf.getContent());
                rf.getRequire().add(dbf);
            }
            result.put("filter."+dbFilter.getId()+".json",mapper.writeValueAsBytes(rf));
        }
        return result;
    }

    @Override
    public String getId() {
        return "js";
    }

    @Override
    public void uploadItems(HashMap<String, byte[]> data) {

    }
}

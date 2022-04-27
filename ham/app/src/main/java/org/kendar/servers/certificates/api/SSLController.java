package org.kendar.servers.certificates.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.events.events.SSLChangedEvent;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.SSLConfig;
import org.kendar.servers.config.SSLDomain;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
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

  public SSLController(JsonConfiguration configuration,
                       EventQueue eventQueue) {

    this.configuration = configuration;
    this.eventQueue = eventQueue;
  }

  @Override
  public String getId() {
    return "org.kendar.servers.certificates.api.SSLController";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/ssl",
      method = "GET",
      id = "1008a4b4-277d-11ec-9621-0242ac130002")
  public void getExtraServers(Request req, Response res) throws JsonProcessingException {
    var domains = configuration.getConfiguration(SSLConfig.class).getDomains();
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(domains));
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/ssl/{id}",
      method = "DELETE",
      id = "1009a4b4-277d-11ec-9621-0242ac130002")
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
      pathAddress = "/api/ssl/swap/{id1}/{id2}",
      method = "PUT",
      id = "1010a4b4-277d-11ec-9621-0242ac130002")
  public void swapDnsServer(Request req, Response res) {
    var cloned = configuration.getConfiguration(SSLConfig.class).copy();
    var domains = cloned.getDomains();
    var name1 = (req.getPathParameter("id1"));
    var name2 = (req.getPathParameter("id2"));

    var id1Index = -1;
    var id2Index = -1;
    for (int i = 0; i < domains.size(); i++) {
      if (domains.get(i).getId().equalsIgnoreCase(name1)) id1Index = i;
      if (domains.get(i).getId().equalsIgnoreCase(name2)) id2Index = i;
    }
    var id1Clone = domains.get(id1Index).copy();
    domains.set(id1Index, domains.get(id2Index));
    domains.set(id2Index, id1Clone);
    configuration.setConfiguration(cloned);
    res.setStatusCode(200);
    eventQueue.handle(new SSLChangedEvent());
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/ssl",
      method = "POST",
      id = "1011a4b4-2ASD77d-11ec-9621-0242ac130002")
  public void addDnsServer(Request req, Response res) throws Exception {
    var cloned = configuration.getConfiguration(SSLConfig.class).copy();

    List<SSLDomain> newList = new ArrayList<>();
    if(req.getRequestText().startsWith("[")){
      newList = (List<SSLDomain>)mapper.readValue(req.getRequestText(), List.class).stream()
              .map(a->{
                var newDomain = new SSLDomain();
                newDomain.setId(UUID.randomUUID().toString());
                newDomain.setAddress((String)a);
                return newDomain;
              }).collect(Collectors.toList());

    }else {
      newList.add( mapper.readValue(req.getRequestText(), SSLDomain.class));
    }
    final var newList2= newList;
    var notNew = cloned.getDomains().stream()
                    .filter(a->!newList2.stream().anyMatch(m->m.getAddress().equalsIgnoreCase(a.getAddress())))
            .collect(Collectors.toList());
    newList.addAll(notNew);
    cloned.setDomains(newList);
    configuration.setConfiguration(cloned);
    res.setStatusCode(200);
    eventQueue.handle(new SSLChangedEvent());
  }
}

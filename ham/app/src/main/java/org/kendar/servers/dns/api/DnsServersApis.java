package org.kendar.servers.dns.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.dns.configurations.DnsConfig;
import org.kendar.dns.configurations.ExtraDnsServer;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class DnsServersApis implements FilteringClass {
  private final JsonConfiguration configuration;
  private final Pattern ipPattern =
      Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
  final ObjectMapper mapper = new ObjectMapper();
  private final DnsMultiResolver dnsMultiResolver;

  public DnsServersApis(JsonConfiguration configuration, DnsMultiResolver dnsMultiResolver) {

    this.configuration = configuration;
    this.dnsMultiResolver = dnsMultiResolver;
  }

  @Override
  public String getId() {
    return "org.kendar.servers.dns.api.DnsApis";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/dns/servers",
      method = "GET",
      id = "1002a4b4-277d-11ec-9621-0242ac130002")
  public void getExtraServers(Request req, Response res) throws JsonProcessingException {
    var dnsServeres = configuration.getConfiguration(DnsConfig.class).getExtraServers();
    res.addHeader("Content-type", "application/json");
    res.setResponseText(mapper.writeValueAsString(dnsServeres));
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/dns/servers/{id}",
      method = "GET",
      id = "1003a4b4-277d-11ec-9621-0242ac130002")
  public void getDnsServer(Request req, Response res) throws JsonProcessingException {
    var dnsServers = configuration.getConfiguration(DnsConfig.class).getExtraServers();
    var name = getIdPathParameter(req, "id");
    for (var item : dnsServers) {
      if (item.getId().equalsIgnoreCase(name)) {

        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(item));
        return ;
      }
    }
    res.setStatusCode(404);
  }

  private String getIdPathParameter(Request req, String id) {
    return req.getPathParameter(id);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/dns/servers/{id}",
      method = "DELETE",
      id = "1004a4b4-277d-11ec-9621-0242ac130002")
  public void removeDnsServer(Request req, Response res) {
    var cloned = configuration.getConfiguration(DnsConfig.class).copy();
    var dnsServeres = cloned.getExtraServers();
    var name = (getIdPathParameter(req, "id"));
    var newList = new ArrayList<ExtraDnsServer>();
    for (var item : dnsServeres) {
      if (item.getId().equalsIgnoreCase(name)) {
        if (item.isEnv()) return;
        continue;
      }
      newList.add(item);
    }
    cloned.setExtraServers(newList);
    configuration.setConfiguration(cloned);
    res.setStatusCode(200);
  }


  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/dns/servers/{id}",
      method = "PUT",
      id = "1005a4b4-277d-11ec-9621-0242ac130002")
  public void updateDnsServer(Request req, Response res) throws Exception {

    var cloned = configuration.getConfiguration(DnsConfig.class).copy();
    var dnsServers = cloned.getExtraServers();
    var id = (getIdPathParameter(req, "id"));
    var newList = new ArrayList<ExtraDnsServer>();
    var newData = mapper.readValue(req.getRequestText(), ExtraDnsServer.class);
    newData.setEnv(false);
    for (var item : dnsServers) {
      var clone = item.copy();
      if (!clone.getId().equalsIgnoreCase(id)) {
        if (clone.isEnv()) return;
        newList.add(clone);
        continue;
      }
      clone.setAddress(newData.getAddress());
      if (prepareResolvedResponse(res, newList, newData, clone)) return;
    }

    for (var item : newList) {
      if (item.getResolved().equalsIgnoreCase(newData.getResolved()))
        throw new Exception("Duplicate dns resolution");
    }
    cloned.setExtraServers(newList);
    configuration.setConfiguration(cloned);
    res.setStatusCode(200);
  }

  private boolean prepareResolvedResponse(Response res, ArrayList<ExtraDnsServer> newList, ExtraDnsServer newData, ExtraDnsServer clone) {
    var resolved = newData.getAddress();
    Matcher ipPatternMatcher = ipPattern.matcher(newData.getAddress());
    if (!ipPatternMatcher.matches()) {
      var allResolved = dnsMultiResolver.resolve(newData.getAddress());
      if (allResolved.isEmpty()) {
        res.setStatusCode(500);
        res.setResponseText("Unable to resolve " + newData.getAddress());
        return true;
      }
      resolved = allResolved.get(0);
    }
    clone.setResolved(resolved);
    newList.add(clone);
    return false;
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/dns/servers/swap/{id1}/{id2}",
      method = "PUT",
      id = "1006a4b4-277d-11ec-9621-0242ac130002")
  public void swapDnsServer(Request req, Response res) {
    var cloned = configuration.getConfiguration(DnsConfig.class).copy();
    var dnsServeres = cloned.getExtraServers();
    var name1 = (getIdPathParameter(req, "id1"));
    var name2 = (getIdPathParameter(req, "id2"));
    var id1Index = -1;
    var id2Index = -1;
    for (int i = 0; i < dnsServeres.size(); i++) {
      if (dnsServeres.get(i).getId().equalsIgnoreCase(name1)) id1Index = i;
      if (dnsServeres.get(i).getId().equalsIgnoreCase(name2)) id2Index = i;
    }
    if (dnsServeres.get(id1Index).isEnv()) return ;
    if (dnsServeres.get(id2Index).isEnv()) return ;
    var id1Clone = dnsServeres.get(id1Index).copy();
    dnsServeres.set(id1Index, dnsServeres.get(id2Index));
    dnsServeres.set(id2Index, id1Clone);
    configuration.setConfiguration(cloned);
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/dns/servers",
      method = "POST",
      id = "1007a4b5-277d-11ec-9621-0242ac130002")
  public void addDnsServer(Request req, Response res) throws Exception {
    var cloned = configuration.getConfiguration(DnsConfig.class).copy();
    var dnsServeres = cloned.getExtraServers();
    var newList = new ArrayList<ExtraDnsServer>();
    var newData = mapper.readValue(req.getRequestText(), ExtraDnsServer.class);
    newData.setEnv(false);

    for (var item : dnsServeres) {
      if (item.getId().equalsIgnoreCase(newData.getId()))
        throw new Exception("Duplicate dns resolution");
      newList.add(item.copy());
    }
    if (prepareResolvedResponse(res, newList, newData, newData)) return ;

    /*for (var item : newList) {
      if (item.getResolved().equalsIgnoreCase(newData.getResolved()))
        throw new Exception("Duplicate dns resolution");
    }*/
    cloned.setExtraServers(newList);
    configuration.setConfiguration(cloned);
    res.setStatusCode(200);
  }
}

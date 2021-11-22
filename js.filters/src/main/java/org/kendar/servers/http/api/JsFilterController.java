package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class JsFilterController implements FilteringClass {
  private final JsonConfiguration configuration;
  ObjectMapper mapper = new ObjectMapper();

  public JsFilterController(JsonConfiguration configuration) {

    this.configuration = configuration;
  }

  @Override
  public String getId() {
    return "org.kendar.servers.http.api.JsFilterController";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/jsfilter",
      method = "GET",
      id = "1000a4b4-277d-11ec-9621-0242ac130002")
  public boolean getDnsMappings(Request req, Response res) {
    return false;
  }
}

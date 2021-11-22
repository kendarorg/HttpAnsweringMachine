package org.kendar.servers.http.api;

import io.swagger.v3.oas.integration.api.OpenApiReader;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.configurations.FilterConfig;
import org.kendar.swagger.SwaggerLoader;

import java.util.ArrayList;

public class SwaggerApi {
  private FilterConfig filtersConfiguration;
  private OpenApiReader openApiReader;
  private JsonConfiguration jsonConfiguration;

  public SwaggerApi(FilterConfig filtersConfiguration, OpenApiReader openApiReader, JsonConfiguration jsonConfiguration){

    this.filtersConfiguration = filtersConfiguration;
    this.openApiReader = openApiReader;
    this.jsonConfiguration = jsonConfiguration;
  }
  public void loadSwagger(){
    var config = filtersConfiguration.get();
    var result = new ArrayList<String>();
    var swloader = new SwaggerLoader();

    for (var kvp : config.filtersByClass.entrySet()) {
      var instance = kvp.getValue().get(0);
      swloader.read(instance.getClass());
    }
    var openapi = swloader.getOpenAPI();
  }
}

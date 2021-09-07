package org.kendar.oidc;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${oidc.address:oidc.local.com}",blocking = true)
public class OidcCors implements FilteringClass {
    @Override
    public String getId() {
        return "org.kendar.oidc.OidcCors";
    }
    @HttpMethodFilter(phase = HttpFilterType.PRE_RENDER,
            pathAddress ="*",
            method = "OPTIONS",
            blocking = true)
    public boolean cors(Request req, Response res) {
        res.getHeaders().put("access-control-allow-credentials","false");
        res.getHeaders().put("access-control-allow-headers","*");
        res.getHeaders().put("access-control-allow-methods","*");
        res.getHeaders().put("access-control-allow-origin","*");
        res.getHeaders().put("content-length","0");
        res.setStatusCode(200);
        return false;
    }
}

package org.kendar.oidc;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",blocking = true)
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
        res.addHeader("access-control-allow-credentials","false");
        res.addHeader("access-control-allow-headers","*");
        res.addHeader("access-control-allow-methods","*");
        res.addHeader("access-control-allow-origin","*");
        res.addHeader("content-length","0");
        res.setStatusCode(200);
        return false;
    }
}

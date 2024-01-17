package org.kendar.oidc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.kendar.http.annotations.SwaggerEnricher;
import org.springframework.stereotype.Component;

@Component
public class OidcSwaggerEnricher implements SwaggerEnricher {
    /**
     * According to <a href="https://swagger.io/docs/specification/authentication/#multipl<a href="e">...</a>
     * ">* https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#se</a>curitySchemeObject
     *
     * @param swagger
     */
    @Override
    public void enrich(OpenAPI swagger) {
        var components = swagger.getComponents();
        if (components == null) {
            components = new Components();
        }
        var securityScheme = new SecurityScheme();
        securityScheme.type(SecurityScheme.Type.HTTP);
        securityScheme.scheme("bearer");
        securityScheme.bearerFormat("Value: Bearer {jwt}");
        securityScheme.in(SecurityScheme.In.HEADER);
        securityScheme.name("Authorization");
        components.addSecuritySchemes("OidcBearer", securityScheme);
        securityScheme = new SecurityScheme();
        securityScheme.type(SecurityScheme.Type.HTTP);
        securityScheme.scheme("basic");
        securityScheme.in(SecurityScheme.In.HEADER);
        securityScheme.name("Authorization");
        components.addSecuritySchemes("OidcBasic", securityScheme);
        swagger.components(components);

        /*var security = swagger.getSecurity();
        if(security==null){
            security = new ArrayList<>();
        }
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.
        security.add(securityRequirement);
        swagger.security(security);*/

    }
}

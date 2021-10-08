This server is based on [Fake OIDC Server on github](https://github.com/CESNET/fake-oidc-serve)

## Apis

It exposes the follwing APIs

* /api/plugins/odic
    * /.well-known/openid-configuration: This URL returns a JSON listing of the OpenID/OAuth endpoints, supported scopes and claims, public keys used to sign the tokens, and other details
    * /authorize: which uses HTTP Basic Auth for asking for username and password
    * /token: for exchanging authorization code for access token
    * /userinfo: that provides data about the user
    * /jwks: providing JSON Web Key Set for validating cryptographic signature of id_token
    * /introspect: that provides access token introspection

<!--
        //KENDAR REMOVED AUTH NEED
        /*if (auth == null) {
            log.info("user and password not provided");
            return response401();
        } else */{
            //String[] creds = new String(Base64.getDecoder().decode(auth.split(" ")[1])).split(":", 2);
-->
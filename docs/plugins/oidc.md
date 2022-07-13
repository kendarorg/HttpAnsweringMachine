This server is based on [Fake OIDC Server on github](https://github.com/CESNET/fake-oidc-serve)

The authorize APIs always return "authorized"

## Apis

It exposes the follwing APIs

* /api/plugins/odic
    * /.well-known/openid-configuration: This URL returns a JSON listing of the OpenID/OAuth endpoints, supported scopes and claims, public keys used to sign the tokens, and other details
    * /authorize: which uses HTTP Basic Auth for asking for username and password
    * /token: for exchanging authorization code for access token
    * /userinfo: that provides data about the user
    * /jwks: providing JSON Web Key Set for validating cryptographic signature of id_token
    * /introspect: that provides access token introspection

## Examples

Authenticate with login and password (any!) top right on the swagger interface

### With token

Call /authorize setting the response_type to "token". To show the response data without redirect
you should fill with any value the redirect parameter.

On the response you could copy the token parameter on the Location

Call /introspect to retrieve the data about the token. Use the token parameter from the /authorize call

Call /userinfo to retrieve the data about the user. Use the token parameter from the /authorize call

### With code

Call /authorize setting the response_type to "code". To show the response data without redirect
you should fill with any value the redirect parameter.

On the response you could copy the token parameter on the Location

Call /token to retrieve the token. It will be in the access_token field


Call /introspect to retrieve the data about the token. Use the token parameter from the /token call

Call /userinfo to retrieve the data about the user. Use the token parameter from the /token call
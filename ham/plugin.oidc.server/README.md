Login with OidcAuthorize
===========
Authorize
response_type: token
redirect: any string

	Retrieve the token

Introspect
token: taken from authorize
UserInfo
token: taken from authorize
============
Authorize
response_type: code
redirect: any string

	Retrieve the code

Token
retrieve the access_token
Introspect
token: taken from Token
UserInfo
token: taken from Token
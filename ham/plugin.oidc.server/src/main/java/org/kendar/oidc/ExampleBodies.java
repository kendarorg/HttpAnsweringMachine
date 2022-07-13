package org.kendar.oidc;

public class ExampleBodies {
    public static final String JWKS_ENDPOINT ="{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",\"kid\":\"rsa1\",\"alg\":\"RS256\",\"n\":\"nVGg2Pw8MK1H6_on7PV8_zphnXVpa6bRcKfF8H61NEqK1rW9JUblyLuv7QNXD7ujC6v6laRVgNPFLZyFjGbU_PyCc3oIKgT9gbllGXMjBhZo70OEQ5uutrK1dTB6kSsX4GRTjxNG6ilb26NEUJZifun5QbjK8Lj0cc4VgfGvUkSLlggv7V45CIYvVQprB5Kbd1gM__xP7MHxaYY4LZNOq0OrxX7f6O7a-LjjHQw4dENCgzyPr4z7mlCIc4rBOKiva9QwKVC-VXlhB0KlYGaBhwt_KZLaGmzSwdZKKhHcEk9kxV9KNcl8gdZegOiq1jtikX_sTLL894tqLdaQXcuXMQ\"}]}";
    public static final String METADATA_ENDPOINT="{\n" +
            "  \"issuer\": \"https://localhost/api/plugins/oidc/\",\n" +
            "  \"authorization_endpoint\": \"https://localhost/api/plugins/oidc/api/plugins/oidc/authorize\",\n" +
            "  \"token_endpoint\": \"https://localhost/api/plugins/oidc/api/plugins/oidc/token\",\n" +
            "  \"userinfo_endpoint\": \"https://localhost/api/plugins/oidc/api/plugins/oidc/userinfo\",\n" +
            "  \"jwks_uri\": \"https://localhost/api/plugins/oidc/api/plugins/oidc/jwks\",\n" +
            "  \"introspection_endpoint\": \"https://localhost/api/plugins/oidc/api/plugins/oidc/introspect\",\n" +
            "  \"scopes_supported\": [\n" +
            "    \"openid\",\n" +
            "    \"profile\",\n" +
            "    \"email\"\n" +
            "  ],\n" +
            "  \"response_types_supported\": [\n" +
            "    \"id_token token\",\n" +
            "    \"code\"\n" +
            "  ],\n" +
            "  \"grant_types_supported\": [\n" +
            "    \"authorization_code\",\n" +
            "    \"implicit\"\n" +
            "  ],\n" +
            "  \"subject_types_supported\": [\n" +
            "    \"public\"\n" +
            "  ],\n" +
            "  \"id_token_signing_alg_values_supported\": [\n" +
            "    \"RS256\",\n" +
            "    \"none\"\n" +
            "  ],\n" +
            "  \"claims_supported\": [\n" +
            "    \"sub\",\n" +
            "    \"iss\",\n" +
            "    \"name\",\n" +
            "    \"family_name\",\n" +
            "    \"given_name\",\n" +
            "    \"preferred_username\",\n" +
            "    \"email\"\n" +
            "  ],\n" +
            "  \"code_challenge_methods_supported\": [\n" +
            "    \"plain\",\n" +
            "    \"S256\"\n" +
            "  ]\n" +
            "}";
    public static final String AUTHORIZATION_ENDPOINT_CODE ="Location: http://localhost/api/remote/mirror?code=j8unx0s-JbEPU_EPUldLKA&state=random_state_string";
    public static final String USERINFO_ENDPOINT = "{\n" +
            "  \"sub\"         : \"83692\",\n" +
            "  \"name\"        : \"Alice Adams\",\n" +
            "  \"given_name\"  : \"Alice\",\n" +
            "  \"family_name\" : \"Adams\",\n" +
            "  \"email\"       : \"alice@example.com\",\n" +
            "  \"picture\"     : \"https://example.com/83692/photo.jpg\"\n" +
            "}";
    public static final String USERINFO_ENDPOINT2 = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEifQ.eyJzdWIiOiJhbGljZSIsImVtYWlsIjoiYWxpY2VAd29u\n" +
            "ZGVybGFuZC5uZXQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IkFsaWNlIEFkYW1zIiwiYXV\n" +
            "kIjoiMDAwMTIzIiwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODBcL2MyaWQiLCJmYW1pbHlfbm\n" +
            "FtZSI6IkFkYW1zIiwiaWF0IjoxNDEzOTg1NDAyLCJncm91cHMiOlsiYWRtaW4iLCJhdWRpdCJdfQ.FJ\n" +
            "v9UnxvQxYvlc2F_v657SIyZkjQ382Bc108O--UFh3cvkjxiO5P2sJyvcqfuGrlzgvU7gCKzTIqqrV74\n" +
            "EcHwGb_xyBUPOKuIJGaDKirBdnPbIXMDGpSqmBQes4tc6L8pkhZfRENIlmkP-KphI3wPd4jtko2HXAd\n" +
            "DFVjzK-FPic";
    public static final String AUTHORIZATION_ENDPOINT_TOKEN = "http://localhost/api/remote/mirror#access_token=eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJyYW5kb20gdGVzdF9jbGllbnRfaWQiLCJzY29wZSI6Im9wZW5pZCIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3RcL2FwaVwvcGx1Z2luc1wvb2lkY1wvIiwiZXhwIjoxNjU3NzkxMjk3LCJpYXQiOjE2NTc3MDQ4OTcsImp0aSI6Ijc3MDEzNjFlLTNhMDMtNGY5Mi1iYjAwLTdiYzcwMmVhMmE5NCJ9.jhKdxI3GBFANz1rIlamSxrYq3n2Bg8-XS6q4aOThMnU0-EsmndbK0imc5D8LOk21spZNxYg5yLoEPt9J-jGw2j2oudKs8fJ_6-GRy20NiqDc-FAi81VIoKAVmWkM0vy46NngkV39JagH0QpVhKD33Qu6iwlnC6pEBElhYXIUB3bASbXuIhu5GKrzzSfkATDg6nD5JUqsSbgvaxBhNs7A3pMz_Ztd_5FjdV0RNu1WW0wDi7Eq3btDNaRLfEIDWVe4YJrIQ7TM4lHPkJEdHuMv9c8CfrrmLfZ8oGd9eGGfnzGmZacKevv3xUenMMDbH91MHNPagYHGP8UdbQhCFhLn0g&token_type=Bearer&state=random_state_string&expires_in=86400&id_token=eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoiSkpVZ0o1YnA1N2lKRFM5Q25fWVhkUSIsInN1YiI6ImYxMzlhN2U1LWEzNGQtNGZlOS1hZjZiLTUwZTFjZDU2NmNkMyIsImF1ZCI6InJhbmRvbSB0ZXN0X2NsaWVudF9pZCIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3RcL2FwaVwvcGx1Z2luc1wvb2lkY1wvIiwiZXhwIjoxNjU3NzkxMjk4LCJpYXQiOjE2NTc3MDQ4OTgsIm5vbmNlIjoiMTIzNDUiLCJqdGkiOiJjMTYxZTJhZS03NjU2LTQzNjYtOWU1My0xMmY1YmZhZWQ5NWYifQ.ARNM5FMDUZzSOrP-2cfjndbhR4UtMBVGjK5hYqgHmYOV0lN9VJ7ImFFyxZ7xX9IokyaW7fAtgXnpnF4lhI9voiVtEs_SG38Aj4HTB8IwDDCpM4pN3lBmYjFGoZgQftIS8mOCPffb0QwMisTEacjy-oI9MOo3cxTSnqU__tUVaFcMDArOyV1XwO2WiA-j0lcn3nNUHR2w2l2coBkz9sB1pKAOSE0xHQCTSzUANtZGWW8vwCtzNUQmSFdf6nz0dJ-XgTgAZ-_ajcDj6tBNT0Dz8rS9B-QX0TiODJ2nFHQOvL6LYh0Bmog76VijOd7xtkGrUGrRROWICS_wV0HmwPQ8UQ";
    public static final String TOKEN_ENDPOINT = "{\n" +
            "  \"access_token\": \"eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJyYW5kb20gdGVzdF9jbGllbnRfaWQiLCJzY29wZSI6InByb2ZpbGUiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0XC9hcGlcL3BsdWdpbnNcL29pZGNcLyIsImV4cCI6MTY1NzgwMDQ1NywiaWF0IjoxNjU3NzE0MDU3LCJqdGkiOiJmYTE1NGE2Ny1mY2RkLTQ2Y2MtYWZhMi1kZmQ5YWI2NmRjNDIifQ.JFLU83xbE0AmU06Ba6bwgwIY2EdwCNicx82hX2RHr4CgSLKbKnpy_l8Kqy0684AEPGgwmK_SWmGhN1OLRBS-lwO-uzffzm42gjR_36F-2qH5ELHRnqzLPxSidQGp-gDjLgpAhJVOrXA41tU5tjlukc-7UI5XJ48tcjWUM5C9CBVZ-ejoyRqpjQ5TAcuWGg5EWJJKpZHkOHaMbLED8i_KPCKjuD_IZFkhbsyCIpyRnrF6PYezq_ujHSIRLmJKbvjPzaHhv6eeH1SQev63HhEp08PtuprN53pPK2JtKAcx4TorEcYbzgr5phKYMxE5lmchPaEUQmB8_KhOxounrwZuiw\",\n" +
            "  \"token_type\": \"Bearer\",\n" +
            "  \"expires_in\": \"86400\",\n" +
            "  \"scope\": \"profile\",\n" +
            "  \"id_token\": \"eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoiMW9vWVIydFZzcHk0bk5WQlo2ajNpUSIsInN1YiI6IjM1YWU1OGNkLTExYjAtNDAzNC1hNDI1LTk2ZDdiYTI1M2YzOSIsImF1ZCI6InJhbmRvbSB0ZXN0X2NsaWVudF9pZCIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3RcL2FwaVwvcGx1Z2luc1wvb2lkY1wvIiwiZXhwIjoxNjU3ODAwNDYwLCJpYXQiOjE2NTc3MTQwNjAsIm5vbmNlIjoiMTIzNDUiLCJqdGkiOiI5NTljMGI1ZC05NzM4LTQzYzUtYjMzMi00NDZjY2IwOGEzNGUifQ.QF59ETnBxDIdQ4VJSnuKqQ8zB3fbFo133Nj3dNquIYIl_9D_KK7QwNwOME-TVWmBUZHPhJfNZD0V8ws048ULsb-oQrMGP6cKjpGcavOwH4Zau2GUhvKZQcoJYLggp-KFletcckngUkYWc1czzOxWksakDerURe1m23DTirRlD-mS_IYZHGP5Se1vWOLjzTIsw5JKLm_SpamvXLFV06IbNuKuWWLpQrczrtbghtURzrxjhsTsfb6duRyeZsqx1lh2c3u7TYXZjWlBFoQD4TAFbnK3v1TufR2w184kdu4k-kT7-_D5WgKbngwBBNLKFYZcP4zfPzPz-MnzVOMRkwvvRw\"\n" +
            "}";
    public static final String INTROSPECTION_ENDPOINT = "{\n" +
            "  \"active\": true,\n" +
            "  \"scope\": \"profile\",\n" +
            "  \"client_id\": \"random test_client_id\",\n" +
            "  \"username\": null,\n" +
            "  \"token_type\": \"Bearer\",\n" +
            "  \"exp\": 1657800457213,\n" +
            "  \"sub\": null,\n" +
            "  \"iss\": \"https://localhost/api/plugins/oidc/\"\n" +
            "}";
}

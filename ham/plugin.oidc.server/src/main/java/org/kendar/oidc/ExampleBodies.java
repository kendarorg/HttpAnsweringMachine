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
    public static final String AUTHORIZATION_ENDPOINT="";
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
}

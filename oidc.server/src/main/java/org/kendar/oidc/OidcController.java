package org.kendar.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",blocking = true)
public class OidcController implements FilteringClass {
    @Override
    public String getId() {
        return "org.kendar.oidc.OidcController";
    }

    @Value("${oidc.token.expiration:86400}")
    private int tokenExpirationSeconds;
    @Value("${localhost.name}")
    private  String serverAddress;
    private Logger log;

    public OidcController(LoggerBuilder loggerBuilder){
        log = loggerBuilder.build(OidcController.class);
    }

    @PostConstruct
    public void postConstruct(){
        log.info("Oidc server LOADED: "+serverAddress);
    }

    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FOUND = 302;


    public static final String METADATA_ENDPOINT = "/api/plugins/oidc/.well-known/openid-configuration";
    public static final String AUTHORIZATION_ENDPOINT = "/api/plugins/oidc/authorize";
    public static final String TOKEN_ENDPOINT = "/api/plugins/oidc/token";
    public static final String USERINFO_ENDPOINT = "/api/plugins/oidc/userinfo";
    public static final String JWKS_ENDPOINT = "/api/plugins/oidc/jwks";
    public static final String INTROSPECTION_ENDPOINT = "/api/plugins/oidc/introspect";

    private JWSSigner signer;
    private JWKSet publicJWKSet;
    private JWSHeader jwsHeader;

    private final Map<String, AccessTokenInfo> accessTokens = new HashMap<>();
    private final Map<String, CodeInfo> authorizationCodes = new HashMap<>();
    private final SecureRandom random = new SecureRandom();



    @PostConstruct
    public void init() throws IOException, ParseException, JOSEException {
        log.info("initializing JWK");
        JWKSet jwkSet = JWKSet.load(getClass().getResourceAsStream("/jwks.json"));
        JWK key = jwkSet.getKeys().get(0);
        signer = new RSASSASigner((RSAKey) key);
        publicJWKSet = jwkSet.toPublicJWKSet();
        jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build();

    }

    /**
     * Provides OIDC metadata. See the spec at https://openid.net/specs/openid-connect-discovery-1_0.html
     */
    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =METADATA_ENDPOINT,method = "GET")
    public boolean metadata(/*UriComponentsBuilder uriBuilder,*/ Request req, Response res) {
        log.info("called " + METADATA_ENDPOINT + " from {}", req.getRemoteHost());
        String urlPrefix = "https://"+serverAddress+"/api/plugins/oidc";
        Map<String, Object> m = new LinkedHashMap<>();
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        // https://tools.ietf.org/html/rfc8414#section-2
        m.put("issuer", urlPrefix + "/"); // REQUIRED
        m.put("authorization_endpoint", urlPrefix + AUTHORIZATION_ENDPOINT); // REQUIRED
        m.put("token_endpoint", urlPrefix + TOKEN_ENDPOINT); // REQUIRED unless only the Implicit Flow is used
        m.put("userinfo_endpoint", urlPrefix + USERINFO_ENDPOINT); // RECOMMENDED
        m.put("jwks_uri", urlPrefix + JWKS_ENDPOINT); // REQUIRED
        m.put("introspection_endpoint", urlPrefix + INTROSPECTION_ENDPOINT);
        m.put("scopes_supported", Arrays.asList("openid", "profile", "email")); // RECOMMENDED
        m.put("response_types_supported", Arrays.asList("id_token token", "code")); // REQUIRED
        m.put("grant_types_supported", Arrays.asList("authorization_code", "implicit")); //OPTIONAL
        m.put("subject_types_supported", Collections.singletonList("public")); // REQUIRED
        m.put("id_token_signing_alg_values_supported", Arrays.asList("RS256", "none")); // REQUIRED
        m.put("claims_supported", Arrays.asList("sub", "iss", "name", "family_name", "given_name", "preferred_username", "email"));
        m.put("code_challenge_methods_supported", Arrays.asList("plain", "S256")); // PKCE support advertised
        setJsonResponse(200,m,res);
        return false;
    }

    /**
     * Provides JSON Web Key Set containing the public part of the key used to sign ID tokens.
     */
    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =JWKS_ENDPOINT,method = "GET")
    public boolean jwks(Request req, Response res) {
        log.info("called " + JWKS_ENDPOINT + " from {}", req.getRemoteHost());
        res.setResponse(publicJWKSet.toString());
        return false;
    }

    /**
     * Provides claims about a user. Requires a valid access token.
     */
    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =USERINFO_ENDPOINT,method = "GET")
    public boolean userinfo(Request req, Response res) {
        var auth = req.getRequestParam("Authorization");
        var access_token = req.getRequestParam("access_token");
        log.info("called " + USERINFO_ENDPOINT + " from {}", req.getRemoteHost());
        if (!auth.startsWith("Bearer ")) {
            if(access_token == null) {
                res.setStatusCode(STATUS_UNAUTHORIZED);
                res.setResponse("No token");
                return false;
            }
            auth = access_token;
        } else {
            auth = auth.substring(7);
        }
        AccessTokenInfo accessTokenInfo = accessTokens.get(auth);
        if (accessTokenInfo == null) {
            res.setStatusCode(STATUS_UNAUTHORIZED);
            res.setResponse("access token not found");
            return false;
        }
        Set<String> scopes = setFromSpaceSeparatedString(accessTokenInfo.scope);
        Map<String, Object> m = new LinkedHashMap<>();
        User user = accessTokenInfo.user;
        m.put("sub", user.getSub());
        if (scopes.contains("profile")) {
            m.put("name", user.getName());
            m.put("family_name", user.getFamily_name());
            m.put("given_name", user.getGiven_name());
            m.put("preferred_username", user.getPreferred_username());
        }
        if (scopes.contains("email")) {
            m.put("email", user.getEmail());
        }
        setJsonResponse(200,m,res);
        return false;
    }

    /**
     * Provides information about a supplied access token.
     */
    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =INTROSPECTION_ENDPOINT,method = "POST")
    public boolean introspection(Request req, Response res) {
        var auth = req.getRequestParam("Authorization");
        var token = req.getRequestParam("token");
        log.info("called " + INTROSPECTION_ENDPOINT + " from {}", req.getRemoteHost());
        Map<String, Object> m = new LinkedHashMap<>();
        AccessTokenInfo accessTokenInfo = accessTokens.get(token);
        if (accessTokenInfo == null) {
            log.error("token not found in memory: {}", token);
            m.put("active", false);
        } else {
            log.info("found token for user {}, releasing scopes: {}", accessTokenInfo.user.getSub(), accessTokenInfo.scope);
            // see https://tools.ietf.org/html/rfc7662#section-2.2 for all claims
            m.put("active", true);
            m.put("scope", accessTokenInfo.scope);
            m.put("client_id", accessTokenInfo.clientId);
            m.put("username", accessTokenInfo.user.getSub());
            m.put("token_type", "Bearer");
            m.put("exp", accessTokenInfo.expiration.toInstant().toEpochMilli());
            m.put("sub", accessTokenInfo.user.getSub());
            m.put("iss", accessTokenInfo.iss);
        }
        setJsonResponse(200,m,res);
        return false;
    }

    /**
     * Provides token endpoint.
     */
    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =TOKEN_ENDPOINT,method = "POST")
    public boolean token(Request req, Response res) throws NoSuchAlgorithmException, JOSEException, NoSuchAlgorithmException {
        var grant_type = req.getRequestParam("grant_type");
        var code = req.getRequestParam("code");
        var redirect_uri = req.getRequestParam("redirect_uri");
        var client_id = req.getRequestParam("client_id");
        var auth = req.getRequestParam("Authorization");
        var code_verifier = req.getRequestParam("code_verifier");
        log.info("called " + TOKEN_ENDPOINT + " from {}, grant_type={} code={} redirect_uri={} client_id={}", req.getRemoteHost(), grant_type, code, redirect_uri, client_id);
        if (!"authorization_code".equals(grant_type)) {
            jsonError(res,"unsupported_grant_type", "grant_type is not authorization_code");
            return false;
        }
        CodeInfo codeInfo = authorizationCodes.get(code);
        if (codeInfo == null) {
            jsonError(res,"invalid_grant", "code not valid");
            return false;
        }
        if (!redirect_uri.equals(codeInfo.redirect_uri)) {
            jsonError(res,"invalid_request", "redirect_uri not valid");
            return false;
        }
        if (codeInfo.codeChallenge != null) {
            // check PKCE
            if(code_verifier == null) {
                jsonError(res,"invalid_request", "code_verifier missing");
                return false;
            }
            if ("S256".equals(codeInfo.codeChallengeMethod)) {
                MessageDigest s256 = MessageDigest.getInstance("SHA-256");
                s256.reset();
                s256.update(code_verifier.getBytes(StandardCharsets.UTF_8));
                String hashedVerifier = Base64URL.encode(s256.digest()).toString();
                if (!codeInfo.codeChallenge.equals(hashedVerifier)) {
                    log.warn("code_verifier {} hashed using S256 to {} does not match code_challenge {}", code_verifier, hashedVerifier, codeInfo.codeChallenge);
                    jsonError(res,"invalid_request", "code_verifier not correct");
                    return false;
                }
                log.info("code_verifier OK");
            } else {
                if (!codeInfo.codeChallenge.equals(code_verifier)) {
                    log.warn("code_verifier {} does not match code_challenge {}", code_verifier, codeInfo.codeChallenge);
                    jsonError(res,"invalid_request", "code_verifier not correct");
                    return false;
                }
            }
        }
        // return access token
        Map<String, String> map = new LinkedHashMap<>();
        String accessToken = createAccessToken(codeInfo.iss, codeInfo.user, codeInfo.client_id, codeInfo.scope);
        map.put("access_token", accessToken);
        map.put("token_type", "Bearer");
        map.put("expires_in", String.valueOf(tokenExpirationSeconds));
        map.put("scope", codeInfo.scope);
        map.put("id_token", createIdToken(codeInfo.iss, codeInfo.user, codeInfo.client_id, codeInfo.nonce, accessToken));

        setJsonResponse(200,map,res);
        return false;
    }


    /**
     * Provides authorization endpoint.
     */
    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =AUTHORIZATION_ENDPOINT,method = "GET")
    public boolean authorize(Request req, Response res) throws JOSEException, NoSuchAlgorithmException {
        var client_id = req.getRequestParam("client_id");
        var redirect_uri = req.getRequestParam("redirect_uri");
        var response_type = req.getRequestParam("response_type");
        var scope = req.getRequestParam("scope");
        var state = req.getRequestParam("state");
        var nonce = req.getRequestParam("nonce");
        var code_challenge = req.getRequestParam("code_challenge");
        var code_challenge_method = req.getRequestParam("code_challenge_method");
        var response_mode = req.getRequestParam("response_mode");
        var auth = req.getRequestParam("Authorization");


        log.info("called " + AUTHORIZATION_ENDPOINT + " from {}, scope={} response_type={} client_id={} redirect_uri={}",
                req.getRemoteHost(), scope, response_type, client_id, redirect_uri);
        //KENDAR REMOVED AUTH NEED
        /*if (auth == null) {
            log.info("user and password not provided");
            return response401();
        } else */{
            //String[] creds = new String(Base64.getDecoder().decode(auth.split(" ")[1])).split(":", 2);
            String login = "test";//creds[0];
            String password = "test";//creds[1];
            User user = new User();//serverProperties.getUser();
            user.setLogname(login);
            user.setPassword(password);
            if (user.getLogname().equals(login) && user.getPassword().equals(password)) {
                log.info("password for user {} is correct", login);
                Set<String> responseType = setFromSpaceSeparatedString(response_type);
                String iss = "https://"+serverAddress +"/api/plugins/oidc/";//uriBuilder.replacePath("/").build().encode().toUriString();
                if (responseType.contains("token")) {
                    // implicit flow
                    log.info("using implicit flow");
                    String access_token = createAccessToken(iss, user, client_id, scope);
                    String id_token = createIdToken(iss, user, client_id, nonce, access_token);
                    String url = redirect_uri + "#" +
                            "access_token=" + urlencode(access_token) +
                            "&token_type=Bearer" +
                            "&state=" + urlencode(state) +
                            "&expires_in=" + tokenExpirationSeconds +
                            "&id_token=" + urlencode(id_token);
                    res.setStatusCode(STATUS_FOUND);
                    res.getHeaders().put("Location",url);
                } else if (responseType.contains("code")) {
                    // authorization code flow
                    log.info("using authorization code flow {}", code_challenge!=null ? "with PKCE" : "");
                    String code = createAuthorizationCode(code_challenge, code_challenge_method, client_id, redirect_uri, user, iss, scope, nonce);
                    String url = redirect_uri + "?" +
                            "code=" + code +
                            "&state=" + urlencode(state);
                    res.setStatusCode(STATUS_FOUND);
                    res.getHeaders().put("Location",url);
                } else {
                    String url = redirect_uri + "#" + "error=unsupported_response_type";
                    res.setStatusCode(STATUS_FOUND);
                    res.getHeaders().put("Location",url);
                }
            } else {
                log.info("wrong user and password combination");
                response401(res);
            }
        }
        return false;
    }

    private String createAuthorizationCode(String code_challenge, String code_challenge_method, String client_id, String redirect_uri, User user, String iss, String scope, String nonce) {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        String code = Base64URL.encode(bytes).toString();
        log.info("issuing code={}", code);
        authorizationCodes.put(code, new CodeInfo(code_challenge, code_challenge_method, code, client_id, redirect_uri, user, iss, scope, nonce));
        return code;
    }

    private String createAccessToken(String iss, User user, String client_id, String scope) throws JOSEException {
        // create JWT claims
        Date expiration = new Date(System.currentTimeMillis() + tokenExpirationSeconds * 1000L);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(expiration)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .build();
        // create JWT token
        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        jwt.sign(signer);
        String access_token = jwt.serialize();
        accessTokens.put(access_token, new AccessTokenInfo(user, access_token, expiration, scope, client_id, iss));
        return access_token;
    }

    private String createIdToken(String iss, User user, String client_id, String nonce, String accessToken) throws NoSuchAlgorithmException, JOSEException {
        // compute at_hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(accessToken.getBytes(StandardCharsets.UTF_8));
        byte[] hashBytes = digest.digest();
        byte[] hashBytesLeftHalf = Arrays.copyOf(hashBytes, hashBytes.length / 2);
        Base64URL encodedHash = Base64URL.encode(hashBytesLeftHalf);
        // create JWT claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + tokenExpirationSeconds * 1000L))
                .jwtID(UUID.randomUUID().toString())
                .claim("nonce", nonce)
                //KENDAR ADDED
                .claim("sub", UUID.randomUUID().toString())
                .claim("at_hash", encodedHash)
                .build();
        // create JWT token
        SignedJWT myToken = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        myToken.sign(signer);
        return myToken.serialize();
    }

    private static String urlencode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static Response response401(Response res) {
        Map<String,String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type","text/html");
        responseHeaders.put("WWW-Authenticate", "Basic realm=\"Fake OIDC server\"");
        res.setResponse("<html><body><h1>401 Unauthorized</h1>Fake OIDC server</body></html>");
        res.setStatusCode(STATUS_UNAUTHORIZED);
        return res;
    }


    private static class AccessTokenInfo {
        final User user;
        final String accessToken;
        final Date expiration;
        final String scope;
        final String clientId;
        final String iss;

        public AccessTokenInfo(User user, String accessToken, Date expiration, String scope, String clientId, String iss) {
            this.user = user;
            this.accessToken = accessToken;
            this.expiration = expiration;
            this.scope = scope;
            this.clientId = clientId;
            this.iss = iss;
        }

    }

    private static class CodeInfo {
        final String codeChallenge;
        final String codeChallengeMethod;
        final String code;
        final String client_id;
        final String redirect_uri;
        final User user;
        final String iss;
        final String scope;
        final String nonce;

        public CodeInfo(String codeChallenge, String codeChallengeMethod, String code, String client_id, String redirect_uri, User user, String iss, String scope, String nonce) {
            this.codeChallenge = codeChallenge;
            this.codeChallengeMethod = codeChallengeMethod;
            this.code = code;
            this.client_id = client_id;
            this.redirect_uri = redirect_uri;
            this.user = user;
            this.iss = iss;
            this.scope = scope;
            this.nonce = nonce;
        }
    }

    private Set<String> setFromSpaceSeparatedString(String s) {
        if (s == null || s.isBlank()) return Collections.emptySet();
        return new HashSet<>(Arrays.asList(s.split(" ")));
    }

    private static ObjectMapper mapper = new ObjectMapper();

    private Response jsonError(Response res,String error, String error_description) {
        log.warn("error={} error_description={}", error, error_description);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("error_description", error_description);
        return setJsonResponse(STATUS_BAD_REQUEST,map,res);
    }

    private static Response setJsonResponse(int code,Object data,Response res){
        res.getHeaders().put("Content-Type","application/json");
        res.setStatusCode(code);
        try {
            var strData = mapper.writeValueAsString(data);
            res.setResponse(strData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return res;
    }
}

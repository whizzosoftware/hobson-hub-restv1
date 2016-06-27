package com.whizzosoftware.hobson.rest.oidc;

import org.jose4j.jwk.RsaJsonWebKey;
import org.json.JSONObject;

public class OIDCConfig {
    private String issuer;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private String jwksEndpoint;
    private String[] responseTypesSupported;
    private String[] grantTypesSupported;
    private String[] subjectTypesSupported;
    private String[] idTokenSigningAlgValuesSupported;
    private RsaJsonWebKey signingKey;

    public OIDCConfig(JSONObject json) {
        this.issuer = json.getString("issuer");
        this.authorizationEndpoint = json.getString("authorization_endpoint");
        this.tokenEndpoint = json.getString("token_endpoint");
        this.userInfoEndpoint = json.getString("userinfo_endpoint");
        this.jwksEndpoint = json.getString("jwks_uri");
    }

    public OIDCConfig(String issuer, String authorizationEndpoint, String tokenEndpoint, String userInfoEndpoint, String jwksEndpoint, RsaJsonWebKey signingKey) {
        this.issuer = issuer;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
        this.userInfoEndpoint = userInfoEndpoint;
        this.jwksEndpoint = jwksEndpoint;
        this.signingKey = signingKey;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public String getJwksEndpoint() {
        return jwksEndpoint;
    }

    public String[] getResponseTypesSupported() {
        return responseTypesSupported;
    }

    public void setResponseTypesSupported(String[] responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    public String[] getSubjectTypesSupported() {
        return subjectTypesSupported;
    }

    public void setSubjectTypesSupported(String[] subjectTypesSupported) {
        this.subjectTypesSupported = subjectTypesSupported;
    }

    public String[] getGrantTypesSupported() {
        return grantTypesSupported;
    }

    public void setGrantTypesSupported(String[] grantTypesSupported) {
        this.grantTypesSupported = grantTypesSupported;
    }

    public String[] getIdTokenSigningAlgValuesSupported() {
        return idTokenSigningAlgValuesSupported;
    }

    public void setIdTokenSigningAlgValuesSupported(String[] idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    }

    public RsaJsonWebKey getSigningKey() {
        return signingKey;
    }
}
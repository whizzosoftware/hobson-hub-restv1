/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.rest.oidc.OIDCConfig;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OIDCConfigTest {
    @Test
    public void testConstructor() {
        JSONObject json = new JSONObject(new JSONTokener("{\"issuer\":\"https://example.org/auth/realms/hobson\",\"authorization_endpoint\":\"https://example.org/auth/realms/hobson/protocol/openid-connect/auth\",\"token_endpoint\":\"https://example.org/auth/realms/hobson/protocol/openid-connect/token\",\"token_introspection_endpoint\":\"https://example.org/auth/realms/hobson/protocol/openid-connect/token/introspect\",\"userinfo_endpoint\":\"https://example.org/auth/realms/hobson/protocol/openid-connect/userinfo\",\"end_session_endpoint\":\"https://example.org/auth/realms/hobson/protocol/openid-connect/logout\",\"jwks_uri\":\"https://example.org/auth/realms/hobson/protocol/openid-connect/certs\",\"grant_types_supported\":[\"authorization_code\",\"implicit\",\"refresh_token\",\"password\",\"client_credentials\"],\"response_types_supported\":[\"code\",\"none\",\"id_token\",\"token\",\"id_token token\",\"code id_token\",\"code token\",\"code id_token token\"],\"subject_types_supported\":[\"public\"],\"id_token_signing_alg_values_supported\":[\"RS256\"],\"response_modes_supported\":[\"query\",\"fragment\",\"form_post\"],\"registration_endpoint\":\"https://example.org/auth/realms/hobson/clients-registrations/openid-connect\"}"));
        OIDCConfig config = new OIDCConfig(json);
        assertEquals("https://example.org/auth/realms/hobson/protocol/openid-connect/auth", config.getAuthorizationEndpoint());
        assertEquals("https://example.org/auth/realms/hobson", config.getIssuer());
        assertEquals("https://example.org/auth/realms/hobson/protocol/openid-connect/certs", config.getJwksEndpoint());
        assertEquals("https://example.org/auth/realms/hobson/protocol/openid-connect/token", config.getTokenEndpoint());
        assertEquals("https://example.org/auth/realms/hobson/protocol/openid-connect/userinfo", config.getUserInfoEndpoint());
    }
}

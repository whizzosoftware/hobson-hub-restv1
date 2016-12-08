/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.rest.oidc.OIDCConfig;
import com.whizzosoftware.hobson.rest.oidc.OIDCConfigProvider;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TokenHelperTest {
    @Test
    public void testCreateToken() throws Exception {
        final RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        final OIDCConfig cfg = new OIDCConfig("issuer", "/authorization", "/token", "/userInfo", "/jwks", rsaJsonWebKey);
        String token = TokenHelper.createToken(new OIDCConfigProvider() {
            @Override
            public OIDCConfig getConfig() {
                return cfg;
            }
        }, new HobsonUser("user1"), Collections.singletonList(HobsonRole.administrator), null);

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setRequireSubject()
                .setExpectedIssuer(cfg.getIssuer())
                .setVerificationKey(cfg.getSigningKey().getKey())
                .setExpectedAudience("hobson-webconsole")
                .build();


        TokenVerification tc = TokenHelper.verifyToken(jwtConsumer, token);
        assertEquals("user1", tc.getUser().getId());
        assertTrue(tc.hasRole(HobsonRole.administrator));
    }
}

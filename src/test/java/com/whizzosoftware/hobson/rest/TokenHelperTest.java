/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.hub.OIDCConfig;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TokenHelperTest {
    @Test
    public void testCreateToken() throws Exception {
        final RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        final OIDCConfig cfg = new OIDCConfig("issuer", "/authorization", "/token", "/userInfo", "/jwks", rsaJsonWebKey);
        String token = TokenHelper.createToken(cfg, new HobsonUser("user1"), Collections.singletonList(HobsonRole.administrator), null);

        HobsonUser tc = TokenHelper.verifyToken(cfg, token);
        assertEquals("user1", tc.getId());
        assertTrue(tc.hasRole(HobsonRole.administrator.name()));
    }
}

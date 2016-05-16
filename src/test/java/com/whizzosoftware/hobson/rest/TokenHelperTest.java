/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import org.junit.Test;
import org.restlet.security.Role;

import static org.junit.Assert.*;

public class TokenHelperTest {
    @Test
    public void testCreateToken() {
        TokenHelper th = new TokenHelper();
        String token = th.createToken("user1", new Role(HobsonRole.ADMIN.name()), null);
        TokenVerification tc = th.verifyToken(token);
        assertEquals("user1", tc.getUser().getId());
        assertTrue(tc.hasRole(HobsonRole.ADMIN.name()));
    }
}

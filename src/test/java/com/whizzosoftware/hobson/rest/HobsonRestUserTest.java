/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.api.user.UserAccount;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HobsonRestUserTest {
    @Test
    public void testNotExpired() {
        long now = System.currentTimeMillis();
        HobsonRestUser u = new HobsonRestUser(new HobsonUser.Builder("id").account(new UserAccount("" + (now + 1), null)).build(), null);
        assertFalse(u.isExpired(now));

        u = new HobsonRestUser(new HobsonUser.Builder("id").account(new UserAccount("" + (now - 1), null)).build(), null);
        assertTrue(u.isExpired(now));
    }
}

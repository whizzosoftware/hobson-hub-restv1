/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.rest.v1.AbstractApiV1Application;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.junit.Test;
import static org.junit.Assert.*;

public class HobsonRestContextTest {
    AbstractApiV1Application app = new AbstractApiV1Application() {
        @Override
        protected String getRealmName() {
            return null;
        }

        @Override
        protected Authorizer createAuthorizer() {
            return null;
        }

        @Override
        protected void createAdditionalResources(Router secureRouter, Router insecureRouter) {
        }
    };

    @Test
    public void testCreateContextWithUserPath() {
        HobsonRestContext ctx = HobsonRestContext.createContext(app, "/api/v1/users/user1");
        assertEquals("user1", ctx.getUserId());
        assertNull(ctx.getHubId());
    }

    @Test
    public void testCreateContextWithUserAndHubPath() {
        HobsonRestContext ctx = HobsonRestContext.createContext(app, "/api/v1/users/user1/hubs/hub1");
        assertEquals("user1", ctx.getUserId());
        assertEquals("hub1", ctx.getHubId());
    }
}

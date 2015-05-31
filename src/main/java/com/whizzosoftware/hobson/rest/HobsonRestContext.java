/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.hub.HubContext;
import org.restlet.Request;
import org.restlet.resource.Resource;
import org.restlet.security.User;

import java.util.Map;

/**
 * A class that captures information about a REST request.
 *
 * @author Dan Noguerol
 */
public class HobsonRestContext {
    private String apiRoot;
    private HubContext hubContext;

    public static HobsonRestContext createContext(Resource resource, String userId, String hubId) {
        return new HobsonRestContext(resource, userId, hubId);
    }

    public static HobsonRestContext createContext(Resource resource, Request request) {
        Map<String,Object> atts = request.getAttributes();

        String userId = "local";
        User user = request.getClientInfo().getUser();
        if (user != null) {
            userId = user.getIdentifier();
        }
        String hubId = (String)atts.get("hubId");
        if (hubId == null) {
            hubId = "local";
        }
        return new HobsonRestContext(resource, userId, hubId);
    }

    protected HobsonRestContext(Resource resource, String userId, String hubId) {
        if (resource != null) {
            this.apiRoot = ((HobsonApiApplication) resource.getApplication()).getApiRoot();
        }
        this.hubContext = HubContext.create(userId, hubId);
    }

    public String getApiRoot() {
        return apiRoot;
    }

    public HubContext getHubContext() {
        return hubContext;
    }

    public String getUserId() {
        return hubContext.getUserId();
    }

    public String getHubId() {
        return hubContext.getHubId();
    }
}

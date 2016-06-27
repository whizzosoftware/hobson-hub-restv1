/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.hub.HubContext;
import org.apache.commons.lang3.StringUtils;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
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
    private String userId;
    private HubContext hubContext;

    public static HobsonRestContext createContext(Application application, ClientInfo clientInfo, String path) {
        String apiRoot = ((HobsonApiApplication)application).getApiRoot();
        path = path.replace(apiRoot, "");
        String[] parts = StringUtils.split(path, '/');
        String userId = clientInfo.getUser().getIdentifier();
        String hubId = null;
        if (parts.length > 1 && parts[0].equals("hubs")) {
            hubId = parts[1];
        }
        return new HobsonRestContext(application, userId, hubId);
    }

    public static HobsonRestContext createContext(Resource resource, ClientInfo clientInfo, String hubId) {
        return new HobsonRestContext(resource.getApplication(), clientInfo.getUser().getIdentifier(), hubId);
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
        return new HobsonRestContext(resource.getApplication(), userId, hubId);
    }

    protected HobsonRestContext(Application application, String userId, String hubId) {
        if (application != null) {
            this.apiRoot = ((HobsonApiApplication)application).getApiRoot();
        }
        this.userId = userId;
        this.hubContext = HubContext.create(hubId);
    }

    public String getApiRoot() {
        return apiRoot;
    }

    public HubContext getHubContext() {
        return hubContext;
    }

    public String getUserId() {
        return userId;
    }

    public String getHubId() {
        return hubContext.getHubId();
    }
}

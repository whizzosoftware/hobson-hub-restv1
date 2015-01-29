/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.resource.Resource;

import java.util.Map;

public class HobsonRestContext {
    private String apiRoot;
    private String userId;
    private String hubId;

    public static HobsonRestContext createContext(Resource resource, Request request) {
        Map<String,Object> atts = request.getAttributes();

        String userId = (String)atts.get("userId");
        if (userId == null) {
            userId = "local";
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
        this.userId = userId;
        this.hubId = hubId;
    }

    public String getApiRoot() {
        return apiRoot;
    }

    public String getUserId() {
        return userId;
    }

    public String getHubId() {
        return hubId;
    }
}

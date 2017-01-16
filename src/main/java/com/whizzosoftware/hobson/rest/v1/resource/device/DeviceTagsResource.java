/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONArray;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class DeviceTagsResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/tags";

    @Inject
    private DeviceManager deviceManager;

    @Override
    protected Representation put(Representation entity) {
        if (!isInRole(HobsonRole.administrator.name()) && !isInRole(HobsonRole.userWrite.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        JSONArray a = JSONHelper.createJSONArrayFromRepresentation(entity);
        Set<String> tags = new HashSet<>();
        for (int i=0; i < a.length(); i++) {
            tags.add(a.getString(i));
        }

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        deviceManager.setDeviceTags(dctx, tags);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

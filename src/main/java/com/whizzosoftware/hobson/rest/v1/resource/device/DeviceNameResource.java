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

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class DeviceNameResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/name";

    @Inject
    AccessManager accessManager;
    @Inject
    DeviceManager deviceManager;

    @Override
    protected Representation put(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DEVICE_CONFIGURE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        deviceManager.setDeviceName(dctx, json.getString("value"));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

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
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class DeviceNameResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/name";

    @Inject
    private DeviceManager deviceManager;

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/devices/:deviceId/name Update device name
     * @apiVersion 0.10.0
     * @apiName SetDeviceName
     * @apiDescription Updates the name of a device.
     * @apiGroup Devices
     * @apiExample Example Request:
     * {
     *   "value": "My Device Name"
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        if (!isInRole(HobsonRole.administrator.name()) && !isInRole(HobsonRole.userWrite.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        deviceManager.setDeviceName(dctx, json.getString("value"));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

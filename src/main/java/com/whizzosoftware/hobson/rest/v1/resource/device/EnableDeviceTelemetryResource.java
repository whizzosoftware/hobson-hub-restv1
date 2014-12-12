/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource that enables/disables device telemetry.
 *
 * @author Dan Noguerol
 */
public class EnableDeviceTelemetryResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/enableTelemetry";

    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/enableTelemetry Enable device telemetry
     * @apiVersion 0.1.8
     * @apiName EnableDeviceTelemetry
     * @apiDescription Enables or disables telemetry for a specific device.
     * @apiGroup Devices
     * @apiParamExample {json} Example Request:
     * {
     *   "value": true
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");

        JSONObject json = JSONMarshaller.createJSONFromRepresentation(entity);

        deviceManager.enableDeviceTelemetry(ctx.getUserId(), ctx.getHubId(), pluginId, deviceId, json.getBoolean("value"));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

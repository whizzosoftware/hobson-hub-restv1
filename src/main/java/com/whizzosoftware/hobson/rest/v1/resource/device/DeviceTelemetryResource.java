/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource that returns device telemetry data.
 *
 * @author Dan Noguerol
 */
public class DeviceTelemetryResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/telemetry";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    TelemetryManager telemetryManager;
    @Inject
    VariableManager variableManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/telemetry Get device variable telemetry
     * @apiVersion 0.1.8
     * @apiName GetDeviceTelemetry
     * @apiDescription Retrieves telemetry for a specific device.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "capable": true,
     *   "enabled": true,
     *   "data": {
     *     "tempF": {
     *       "1408390215763": 72.0
     *     }
     *   },
     *   {
     *     "targetTempF": {
     *       "1408390215763": 73.0
     *     }
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), pluginId, deviceId);
        long endTime = System.currentTimeMillis() / 1000; // TODO: should be pulled from request
        TelemetryInterval interval = TelemetryInterval.HOURS_24; // TODO: should be pulled from request

        HobsonDevice device = deviceManager.getDevice(dctx);

        return new JsonRepresentation(
            linkHelper.addTelemetryLinks(
                ctx,
                pluginId,
                deviceId,
                JSONSerializationHelper.createTelemetryJSON(
                    device.isTelemetryCapable(),
                    telemetryManager.isDeviceTelemetryEnabled(dctx),
                    telemetryManager.getDeviceTelemetry(
                            dctx,
                            endTime,
                            interval
                    )
                )
            )
        );
    }

    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        if (json.has("enabled")) {
            telemetryManager.enableDeviceTelemetry(DeviceContext.create(ctx.getHubContext(), pluginId, deviceId), json.getBoolean("enabled"));
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.api.variable.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.variable.telemetry.TemporalValue;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;

/**
 * A REST resource that returns device telemetry data.
 *
 * @author Dan Noguerol
 */
public class DeviceTelemetryResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/telemetry";

    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/telemetry Get device variable telemetry
     * @apiVersion 0.1.8
     * @apiName GetDeviceTelemetry
     * @apiDescription Retrieves telemetry for a specific device variable.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "tempF": [
     *     {
     *       "time": 1408390215763,
     *       "value": 72.0
     *     }
     *   ]
     * },
     * {
     *   "targetTempF": [
     *     {
     *       "time": 1408390215763,
     *       "value": 73.0
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");

        HobsonDevice device = deviceManager.getDevice(ctx.getUserId(), ctx.getHubId(), pluginId, deviceId);
        String[] varNames = device.getTelemetryVariableNames();
        JSONObject results = new JSONObject();

        if (varNames != null) {
            for (String varName : varNames) {
                Collection<TemporalValue> telemetry = variableManager.getDeviceVariableTelemetry(
                    ctx.getUserId(),
                    ctx.getHubId(),
                    pluginId,
                    deviceId,
                    varName,
                    System.currentTimeMillis() - 86400000, // TODO: should be pulled from request
                    TelemetryInterval.HOURS_24 // TODO: should be pulled from request
                );

                JSONArray seriesArray = new JSONArray();
                results.put(varName, seriesArray);

                for (TemporalValue value : telemetry) {
                    JSONObject json = new JSONObject();
                    json.put("time", value.getTime());
                    json.put("value", value.getValue());
                    seriesArray.put(json);
                }
            }
        }

        return new JsonRepresentation(results);
    }
}

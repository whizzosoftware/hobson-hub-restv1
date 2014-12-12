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
import com.whizzosoftware.hobson.api.variable.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.variable.telemetry.TemporalValue;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

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
     *   "tempF": {
     *     "1408390215763": 72.0
     *   }
     * },
     * {
     *   "targetTempF": {
     *     "1408390215763": 73.0
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");
        long endTime = System.currentTimeMillis() / 1000; // TODO: should be pulled from request
        TelemetryInterval interval = TelemetryInterval.HOURS_24; // TODO: should be pulled from request

        Map<String,Collection<TemporalValue>> telemetry = deviceManager.getDeviceTelemetry(
            ctx.getUserId(),
            ctx.getHubId(),
            pluginId,
            deviceId,
            endTime,
            interval
        );

        JSONObject results = new JSONObject();

        for (String varName : telemetry.keySet()) {
            Collection<TemporalValue> varTm = telemetry.get(varName);

            JSONObject seriesJSON = new JSONObject();
            results.put(varName, seriesJSON);

            for (TemporalValue value : varTm) {
                Double d = (Double)value.getValue();
                if (d != null && !d.equals(Double.NaN)) {
                    seriesJSON.put(Long.toString(value.getTime()), d);
                }
            }
        }

        return new JsonRepresentation(results);
    }
}

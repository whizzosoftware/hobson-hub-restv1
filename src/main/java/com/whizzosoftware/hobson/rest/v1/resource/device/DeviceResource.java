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
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.HobsonVariableCollection;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A REST resource that returns device information.
 *
 * @author Dan Noguerol
 */
public class DeviceResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId Get device details
     * @apiParam {Boolean} variables If true, then include all device variables in the response
     * @apiVersion 0.1.3
     * @apiName GetDeviceDetails
     * @apiDescription Retrieves the details of a specific device.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": "RadioRa Zone 1",
     *   "type": "LIGHTBULB",
     *   "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-radiora",
     *   "telemetryEnabled": false,
     *   "preferredVariable": {
     *     "name": "on",
     *     "value": false,
     *     "links": {
     *       "self": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables/on"
     *     }
     *   },
     *   "links": {
     *     "self": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1",
     *     "config": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/config",
     *     "variables": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        HobsonDevice device = deviceManager.getDevice(dctx);
        boolean telemetryEnabled = deviceManager.isDeviceTelemetryEnabled(dctx);

        // get device variables if request asked for them
        HobsonVariableCollection variables = null;
        if (Boolean.parseBoolean(getQueryValue("variables"))) {
            variables = variableManager.getDeviceVariables(dctx);
        }

        return new JsonRepresentation(
            linkHelper.addDeviceLinks(
                ctx,
                JSONSerializationHelper.createDeviceJSON(
                    ctx.getUserId(),
                    ctx.getHubId(),
                    device,
                    variables,
                    telemetryEnabled,
                    true
                ),
                device,
                true
            )
        );
    }
}

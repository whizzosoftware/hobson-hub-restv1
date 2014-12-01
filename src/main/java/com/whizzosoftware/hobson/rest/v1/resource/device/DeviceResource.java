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
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource that returns device information.
 *
 * @author Dan Noguerol
 */
public class DeviceResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;

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
     *   "preferredVariable": {
     *     "name": "on",
     *     "value": false,
     *     "links": {
     *       "self": "/api/plugins/com.whizzosoftware.hobson.server-radiora/devices/1/variables/on"
     *     }
     *   },
     *   "links": {
     *     "self": "/api/plugins/com.whizzosoftware.hobson.server-radiora/devices/1",
     *     "config": "/api/plugins/com.whizzosoftware.hobson.server-radiora/devices/1/config",
     *     "set-name": "/api/plugins/com.whizzosoftware.hobson.server-radiora/devices/1/name",
     *     "variables": "/api/plugins/com.whizzosoftware.hobson.server-radiora/devices/1/variables"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(JSONMarshaller.createDeviceJSON(ctx, variableManager, deviceManager.getDevice(ctx.getUserId(), ctx.getHubId(), getAttribute("pluginId"), getAttribute("deviceId")), true, Boolean.parseBoolean(getQueryValue("variables"))));
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

/**
 * A REST resource that accesses a device's configuration.
 *
 * @author Dan Noguerol
 */
public class DeviceConfigurationResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/configuration";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/configuration Get device configuration
     * @apiVersion 0.1.3
     * @apiName GetDeviceConfig
     * @apiDescription Retrieves the current configuration for a device.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": {
     *     "name": "Name",
     *     "description": "The device name",
     *     "value": "My Device",
     *     "type": "STRING"
     *   },
     *   "username": {
     *     "name": "User Name",
     *     "description": "A username",
     *     "type": "STRING"
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        return new JsonRepresentation(JSONSerializationHelper.createDeviceConfigurationJSON(deviceManager.getDeviceConfiguration(dctx)));
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/configuration Set device configuration
     * @apiVersion 0.1.3
     * @apiName SetDeviceConfig
     * @apiDescription Sets the current configuration for a device.
     * @apiGroup Devices
     * @apiParamExample {json} Example Request:
     * {
     *   "name": {
     *     "value": "My New Device Name"
     *   },
     *   "username": {
     *     "value": "johndoe"
     *   }
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        try {
            HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
            authorizer.authorizeHub(ctx.getHubContext());
            Map<String,Object> props = JSONSerializationHelper.createConfigurationPropertyMap(new JSONObject(new JSONTokener(entity.getStream())));
            DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
            deviceManager.setDeviceConfigurationProperties(dctx, props, true);
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error setting device property", e);
        }
    }
}

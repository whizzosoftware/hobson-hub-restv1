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
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import org.json.JSONArray;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for obtaining a plugin's devices.
 *
 * @author Dan Noguerol
 */
public class PluginDevicesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices Get all plugin devices
     * @apiVersion 0.1.3
     * @apiParam {Boolean} details If true, include details for each device
     * @apiName GetAllPluginDevices
     * @apiDescription Retrieves a summary list of devices published by a specific plugin.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "id": "1",
     *     "name": "RadioRa Zone 1",
     *     "type": "LIGHTBULB",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/1"
     *     }
     *   },
     *   {
     *     "id": "2",
     *     "name": "RadioRa Zone 2",
     *     "type": "LIGHTBULB",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/2"
     *     }
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        JSONArray results = new JSONArray();
        boolean details = Boolean.parseBoolean(getQueryValue("details"));
        for (HobsonDevice device : deviceManager.getAllDevices(PluginContext.create(ctx.getHubContext(), getAttribute("pluginId")))) {
            results.put(
                linkHelper.addDeviceLinks(
                    ctx,
                    JSONSerializationHelper.createDeviceJSON(
                        ctx.getUserId(),
                        ctx.getHubId(),
                        device,
                        null,
                        details,
                        false
                    ),
                    device,
                    details
                )
            );
        }
        return new JsonRepresentation(results);
    }
}

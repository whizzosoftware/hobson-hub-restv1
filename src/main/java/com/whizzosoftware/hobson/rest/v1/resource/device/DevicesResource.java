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
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import org.json.JSONArray;
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
public class DevicesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/devices";
    public static final String REL = "devices";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/devices Get all devices
     * @apiVersion 0.1.3
     * @apiParam {Boolean} details If true, include details for each device
     * @apiName GetAllDevices
     * @apiDescription Retrieves a summary list of devices published by all plugins.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "RadioRa Zone 1",
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-radiora",
     *     "type": "LIGHTBULB",
     *     "preferredVariable": {
     *       "name": "on",
     *       "value": false,
     *       "links": {
     *         "self": "/api/plugins/v1/users/local/hubs/local/com.whizzosoftware.hobson.server-radiora/devices/1/variables/on"
     *       }
     *     },
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/1",
     *       "setName": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/1/name",
     *       "variables": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/1/variables"
     *     }
     *   },
     *   {
     *     "name": "RadioRa Zone 2",
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-radiora",
     *     "type": "LIGHTBULB",
     *     "preferredVariable": {
     *       "name": "on",
     *       "value": true,
     *       "links": {
     *         "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/2/variables/on"
     *       }
     *     },
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/2",
     *       "setName": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/2/name",
     *       "variables": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/2/variables"
     *     }
     *   }
     * ]
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        JSONArray results = new JSONArray();
        boolean details = Boolean.parseBoolean(getQueryValue("details"));
        for (HobsonDevice device : deviceManager.getAllDevices(ctx.getUserId(), ctx.getHubId())) {
            results.put(linkHelper.addDeviceLinks(
                ctx,
                JSONSerializationHelper.createDeviceJSON(
                    ctx.getUserId(),
                    ctx.getHubId(),
                    variableManager,
                    device,
                    null,
                    details,
                    false
                ),
                device,
                details
            ));
        }
        return new JsonRepresentation(results);
    }
}

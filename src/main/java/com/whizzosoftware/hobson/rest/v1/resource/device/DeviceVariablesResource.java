/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.MediaVariableProxyProvider;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;

/**
 * A REST resource that retrieves device variable information.
 *
 * @author Dan Noguerol
 */
public class DeviceVariablesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/variables";

    @Inject
    Authorizer authorizer;
    @Inject
    VariableManager variableManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/variables Get all device variables
     * @apiVersion 0.1.3
     * @apiName GetAllDeviceVariables
     * @apiDescription Retrieves a summary list of all variables for a specific device.
     * @apiGroup Variables
     * @apiSuccessExample {json} Success Response:
     * {
     *   "on": {
     *     "value": false,
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/9/variables/on"
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
        JSONObject results = new JSONObject();
        Collection<HobsonVariable> variables = variableManager.getDeviceVariables(
            DeviceContext.create(ctx.getHubContext(), pluginId, deviceId),
            new MediaVariableProxyProvider(ctx)
        ).getCollection();
        for (HobsonVariable v : variables) {
            results.put(
                v.getName(),
                JSONSerializationHelper.createDeviceVariableJSON(
                    pluginId,
                    deviceId,
                    v,
                    false
                )
            );
        }
        return new JsonRepresentation(results);
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.event.EventManager;
import com.whizzosoftware.hobson.api.event.VariableUpdateRequestEvent;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Template;

import javax.inject.Inject;

/**
 * A REST resource that manages a device variable.
 *
 * @author Dan Noguerol
 */
public class DeviceVariableResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/variables/{variableName}";

    @Inject
    Authorizer authorizer;
    @Inject
    VariableManager variableManager;
    @Inject
    EventManager eventManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/variables/:variableName Get device variable
     * @apiVersion 0.1.3
     * @apiName GetDeviceVariable
     * @apiDescription Retrieves details for a specific device variable.
     * @apiGroup Variables
     * @apiSuccessExample {json} Success Response:
     * {
     *   "lastUpdate": 1408390215763,
     *   "mask": "WRITE_ONLY",
     *   "links": {
     *     "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/9/variables/level"
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");
        HobsonVariable var = variableManager.getDeviceVariable(DeviceContext.create(ctx.getHubContext(), pluginId, deviceId), getAttribute("variableName"));
        return new JsonRepresentation(
            linkHelper.addDeviceVariableLinks(
                ctx,
                JSONSerializationHelper.createDeviceVariableJSON(
                    pluginId,
                    deviceId,
                    linkHelper.createMediaVariableOverride(ctx, pluginId, deviceId, var),
                    true
                ),
                pluginId,
                deviceId,
                var.getName()
            )
        );
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/variables/:variableName Update device variable
     * @apiVersion 0.1.3
     * @apiName SetDeviceVariable
     * @apiDescription Updates the value of a specific device variable.
     * @apiGroup Variables
     * @apiExample Example Request:
     * {
     *   "value": true
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        Object value = JSONSerializationHelper.createDeviceVariableValue(JSONHelper.createJSONFromRepresentation(entity));
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");
        String variableName = getAttribute("variableName");
        eventManager.postEvent(ctx.getHubContext(), new VariableUpdateRequestEvent(new VariableUpdate(pluginId, deviceId, variableName, value)));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);

        // TODO: is there a better way to do this? The Restlet request reference scheme is always HTTP for some reason...
        Reference requestRef = getRequest().getResourceRef();
        if (Boolean.getBoolean(System.getProperty("useSSL"))) {
            getResponse().setLocationRef(new Reference("https", requestRef.getHostDomain(), requestRef.getHostPort(), ctx.getApiRoot() + new Template(DeviceVariableResource.PATH).format(linkHelper.createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "variableName", variableName)), null, null));
        } else {
            getResponse().setLocationRef(requestRef);
        }

        return new EmptyRepresentation();
    }
}

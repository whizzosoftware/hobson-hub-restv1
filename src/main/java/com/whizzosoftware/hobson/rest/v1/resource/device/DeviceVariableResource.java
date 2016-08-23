/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.event.EventManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.variable.DeviceVariable;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MapUtil;
import io.netty.util.concurrent.Future;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.MediaType;
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
    public static final String PATH = "/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/variables/{variableName}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    EventManager eventManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/variables/:variableName Get device variable
     * @apiVersion 0.1.3
     * @apiName GetDeviceVariable
     * @apiDescription Retrieves details for a specific device variable.
     * @apiGroup Variables
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/9/variables/level",
     *   "lastUpdate": 1408390215763,
     *   "name": "level",
     *   "mask": "WRITE_ONLY",
     *   "value": 100
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute(JSONAttributes.PLUGIN_ID), getAttribute(JSONAttributes.DEVICE_ID));
        DeviceVariable var = deviceManager.getDeviceVariable(DeviceVariableContext.create(dctx, getAttribute(JSONAttributes.VARIABLE_NAME)));

        HobsonVariableDTO dto = new HobsonVariableDTO.Builder(
            idProvider.createDeviceVariableId(var.getContext()),
            var,
            true
        ).build();

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getJSONMediaType()));
        return jr;
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
        final HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute(JSONAttributes.PLUGIN_ID), getAttribute(JSONAttributes.DEVICE_ID));

        Object value = createDeviceVariableValue(JSONHelper.createJSONFromRepresentation(entity));
        final String pluginId = getAttribute("pluginId");
        final String deviceId = getAttribute("deviceId");
        final String variableName = getAttribute("variableName");

        Response response = getResponse();

        try {
            Future f = deviceManager.setDeviceVariable(DeviceVariableContext.create(dctx, variableName), value).await();
            if (f.isSuccess()) {
                response.setStatus(Status.SUCCESS_ACCEPTED);

                // TODO: is there a better way to do this? The Restlet request reference scheme is always HTTP for some reason...
                Reference requestRef = getRequest().getResourceRef();
                if (Boolean.getBoolean(System.getProperty("useSSL"))) {
                    response.setLocationRef(new Reference("https", requestRef.getHostDomain(), requestRef.getHostPort(), ctx.getApiRoot() + new Template(DeviceVariableResource.PATH).format(MapUtil.createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "variableName", variableName)), null, null));
                } else {
                    response.setLocationRef(requestRef);
                }
            } else {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, f.cause());
            }
        } catch (InterruptedException e) {
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }

        return new EmptyRepresentation();
    }

    private Object createDeviceVariableValue(JSONObject json) {
        try {
            return json.get("value");
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }
}

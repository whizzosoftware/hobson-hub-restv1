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
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

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
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/configuration Get device configuration
     * @apiVersion 0.1.3
     * @apiName GetDeviceConfig
     * @apiDescription Retrieves the current configuration for a device.
     * @apiGroup Devices
     * @apiSuccess {Object} cclass The configuration class associated with the configuration
     * @apiSuccess {Object} values The configuration values
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/device1/configuration"
     *   "cclass": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/device1/configurationClass"
     *   },
     *   "values": {
     *     "name": "My Device",
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        PropertyContainer config = deviceManager.getDeviceConfiguration(dctx);

        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(linkProvider.createDeviceConfigurationLink(dctx))
            .name(config.getName())
            .containerClass(
                new PropertyContainerClassDTO.Builder(linkProvider.createDeviceConfigurationClassLink(dctx)).build()
            )
            .values(config.getPropertyValues())
            .build();

        return new JsonRepresentation(dto.toJSON());
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/configuration Set device configuration
     * @apiVersion 0.1.3
     * @apiName SetDeviceConfig
     * @apiDescription Sets the current configuration for a device.
     * @apiGroup Devices
     * @apiParamExample {json} Example Request:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/device1/configuration",
     *   "cclass": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/device1/configurationClass"
     *   },
     *   "values": {
     *     "name": "My New Device Name",
     *   }
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        PropertyContainerDTO dto = new PropertyContainerDTO(JSONHelper.createJSONFromRepresentation(entity));

        deviceManager.setDeviceConfigurationProperties(dctx, dto.getValues(), true);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

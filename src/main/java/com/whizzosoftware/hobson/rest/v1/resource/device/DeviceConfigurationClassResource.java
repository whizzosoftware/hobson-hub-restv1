/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOHelper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class DeviceConfigurationClassResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/configurationClass";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/configurationClass Get device configuration class
     * @apiVersion 0.1.3
     * @apiName GetDeviceConfigClass
     * @apiDescription Retrieves the configuration class for a device.
     * @apiGroup Devices
     * @apiSuccess {Array} supportedProperties A list of configuration properties supported by the device.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/device1/configurationClass"
     *   "supportedProperties": [
     *     {
     *       "id": "name",
     *       "description": "A descriptive name for this device",
     *       "name": "Name",
     *       "type": "STRING"
     *     }
     *   ],
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));

        HobsonDevice device = deviceManager.getDevice(dctx);
        PropertyContainerClass pcc = device.getConfigurationClass();

        PropertyContainerClassDTO dto = new PropertyContainerClassDTO.Builder(linkProvider.createDeviceConfigurationClassLink(dctx))
            .name(pcc.getName())
            .descriptionTemplate(pcc.getDescriptionTemplate())
            .supportedProperties(DTOHelper.mapTypedPropertyList(pcc.getSupportedProperties()))
            .build();

        return new JsonRepresentation(dto.toJSON());
    }
}

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
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
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
    public static final String PATH = "/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId Get device details
     * @apiVersion 0.1.3
     * @apiName GetDeviceDetails
     * @apiDescription Retrieves the details of a specific device.
     * @apiGroup Devices
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "configuration", "configurationClass", "preferredVariable", "variables").
     * @apiSuccess {String} name The device name.
     * @apiSuccess {String} type The device type.
     * @apiSuccess {Object} configuration The current configuration values for the device.
     * @apiSuccess {Object} configurationClass The device's configuration class.
     * @apiSuccess {Object} preferredVariable The device's "preferred variable" if it has one.
     * @apiSuccess {Object} variables The variables the device has published.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": "RadioRa Zone 1",
     *   "type": "LIGHTBULB",
     *   "configuration": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/configuration"
     *   },
     *   "configurationClass": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/configurationClass"
     *   },
     *   "preferredVariable": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables/on"
     *   },
     *   "variables": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        HobsonDeviceDTO dto = new HobsonDeviceDTO.Builder(
            dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
            dctx,
            true
        ).build();

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getJSONMediaType()));
        return jr;
    }
}

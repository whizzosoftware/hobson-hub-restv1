/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.device.DevicePassport;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.device.DevicePassportDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class DevicePassportResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/devicePassports/{passportId}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/devicePassports/{passportId} Get device passport
     * @apiVersion 0.5.0
     * @apiName GetDevicePassport
     * @apiDescription Retrieves a device passport that has been created.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/devicePassports/30a34e54-50c0-11e5-885d-feff819cdc9f",
     *   "deviceId": "aG12Jca",
     *   "creationTime": 0,
     *   "activationTime": 0
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue(JSONAttributes.EXPAND));

        DevicePassport db = deviceManager.getDevicePassport(ctx.getHubContext(), getAttribute("passportId"));

        if (db != null) {
            DevicePassportDTO dto = new DevicePassportDTO.Builder(
                dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                db,
                true,
                false
            ).build();

            JsonRepresentation js = new JsonRepresentation(dto.toJSON());
            js.setMediaType(new MediaType(dto.getJSONMediaType()));
            return js;
        } else {
            throw new HobsonNotFoundException("Device passport not found");
        }
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/devicePassports/{passportId} Reset device passport
     * @apiVersion 0.5.0
     * @apiName ResetDevicePassport
     * @apiDescription Resets device passport to its initially created state.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        deviceManager.resetDevicePassport(ctx.getHubContext(), getAttribute("passportId"));
        return new EmptyRepresentation();
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/deviceaPassports/{passportId} Delete device passport
     * @apiVersion 0.5.0
     * @apiName DeleteDevicePassport
     * @apiDescription Deletes the device passport.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        deviceManager.deleteDevicePassport(ctx.getHubContext(), getAttribute("passportId"));
        return new EmptyRepresentation();
    }
}

/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.device.DeviceBootstrap;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.IdProvider;
import com.whizzosoftware.hobson.dto.device.DeviceBootstrapDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class DeviceBootstrapResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/deviceBootstraps/{bootstrapId}";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/deviceBootstraps/{bootstrapId} Get device bootstrap
     * @apiVersion 0.5.0
     * @apiName GetDeviceBootstrap
     * @apiDescription Retrieves a device bootstrap that has been created.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/deviceBootstraps/30a34e54-50c0-11e5-885d-feff819cdc9f",
     *   "deviceId": "aG12Jca",
     *   "creationTime": 0,
     *   "bootstrapTime": 0
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceBootstrap db = deviceManager.getDeviceBootstrap(ctx.getHubContext(), getAttribute("bootstrapId"));

        if (db != null) {
            DeviceBootstrapDTO dto = new DeviceBootstrapDTO.Builder(
                idProvider.createDeviceBootstrapId(ctx.getHubContext(), db.getId()),
                db,
                true,
                false
            ).build();

            JsonRepresentation js = new JsonRepresentation(dto.toJSON());
            js.setMediaType(new MediaType(dto.getJSONMediaType()));
            return js;
        } else {
            throw new HobsonNotFoundException("Device bootstrap not found");
        }
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/deviceBootstraps/{bootstrapId} Reset device bootstrap
     * @apiVersion 0.5.0
     * @apiName ResetDeviceBootstrap
     * @apiDescription Resets the device bootstrap to its initially created state.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        deviceManager.resetDeviceBootstrap(ctx.getHubContext(), getAttribute("bootstrapId"));
        return new EmptyRepresentation();
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/deviceBootstraps/{bootstrapId} Delete device bootstrap
     * @apiVersion 0.5.0
     * @apiName DeleteDeviceBootstrap
     * @apiDescription Deletes the device bootstrap.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        deviceManager.deleteDeviceBootstrap(ctx.getHubContext(), getAttribute("bootstrapId"));
        return new EmptyRepresentation();
    }
}

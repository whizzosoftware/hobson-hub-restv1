/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.device.DeviceBootstrap;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.device.DeviceBootstrapDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class RegisterDeviceResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/registerDevice";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/registerDevice Register device
     * @apiVersion 0.5.0
     * @apiName RegisterDeviceBootstrap
     * @apiDescription Registers a device for which a bootstrap has previously been created.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * @apiParamExample {json} Example Request:
     * {
     *   "deviceId": "myDeviceId"
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 201 Created
     * {
     *   "@id": "/api/v1/users/local/hubs/local/deviceBootstraps/30a34e54-50c0-11e5-885d-feff819cdc9f",
     *   "deviceId": "aG12Jca",
     *   "creationTime": 1441118757,
     *   "bootstrapTime": 1441119000,
     *   "secret": "0e15dc1a50b811e5885dfeff819cdc9f"
     * }
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
        DeviceBootstrap db = deviceManager.registerDeviceBootstrap(ctx.getHubContext(), json.getString("deviceId"));

        if (db != null) {
            return new JsonRepresentation(
                new DeviceBootstrapDTO.Builder(linkProvider.createDeviceBootstrapLink(ctx.getHubContext(), db.getId()))
                    .deviceId(db.getId())
                    .creationTime(db.getCreationTime())
                    .bootstrapTime(db.getBootstrapTime())
                    .secret(db.getSecret())
                    .build()
                    .toJSON()
            );
        } else {
            throw new HobsonInvalidRequestException("Unable to register device");
        }
    }
}

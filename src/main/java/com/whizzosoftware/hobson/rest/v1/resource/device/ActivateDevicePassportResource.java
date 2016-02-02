/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.device.DevicePassportAlreadyActivatedException;
import com.whizzosoftware.hobson.api.device.DevicePassport;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.device.DevicePassportDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class ActivateDevicePassportResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/activatePassport";

    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/activatePassport Activate device passport
     * @apiVersion 0.5.0
     * @apiName ActivateDevicePassport
     * @apiDescription Activates a device passport that has previously been created.
     * @apiGroup Devices
     * @apiParamExample {json} Example Request:
     * {
     *   "deviceId": "myDeviceId"
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 201 Created
     * {
     *   "@id": "/api/v1/users/local/hubs/local/devicePassports/30a34e54-50c0-11e5-885d-feff819cdc9f",
     *   "deviceId": "aG12Jca",
     *   "creationTime": 1441118757,
     *   "activationTime": 1441119000,
     *   "secret": "0e15dc1a50b811e5885dfeff819cdc9f"
     * }
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue(JSONAttributes.EXPAND));

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
        DevicePassport db = deviceManager.activateDevicePassport(ctx.getHubContext(), json.getString(JSONAttributes.DEVICE_ID));

        try {
            DevicePassportDTO dto = new DevicePassportDTO.Builder(
                dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                db,
                true,
                true
            ).build();
            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
            jr.setMediaType(new MediaType(dto.getJSONMediaType()));
            return jr;
        } catch (DevicePassportAlreadyActivatedException e) {
            throw new HobsonInvalidRequestException("Device passport has already been activated");
        }
    }
}

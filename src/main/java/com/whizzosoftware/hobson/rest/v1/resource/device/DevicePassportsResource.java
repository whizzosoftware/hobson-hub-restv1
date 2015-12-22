/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.device.DevicePassport;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.device.DevicePassportDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;

public class DevicePassportsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/devicePassports";

    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/devicePassports Get all device passports
     * @apiVersion 0.5.0
     * @apiName GetAllDevicePassports
     * @apiDescription Retrieves a list of all device passports that have been created.
     * @apiGroup Devices
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (the only supported value is "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/devicePassports/f8a1e312-50bf-11e5-885d-feff819cdc9f",
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/devicePassports/30a34e54-50c0-11e5-885d-feff819cdc9f",
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue(JSONAttributes.EXPAND));
        boolean itemExpand = expansions.has(JSONAttributes.ITEM);

        Collection<DevicePassport> passports = deviceManager.getDevicePassports(ctx.getHubContext());
        ItemListDTO results = new ItemListDTO(idProvider.createDevicePassportsId(ctx.getHubContext()), true);

        expansions.pushContext(JSONAttributes.ITEM);

        for (DevicePassport db : passports) {
            results.add(new DevicePassportDTO.Builder(idProvider.createDevicePassportId(ctx.getHubContext(), db.getId()), db, itemExpand, false).build());
        }

        expansions.popContext();

        JsonRepresentation jr = new JsonRepresentation(results.toJSON());
        jr.setMediaType(new MediaType(results.getJSONMediaType()));
        return jr;
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/devicePassports Create device passport
     * @apiVersion 0.5.0
     * @apiName CreateDevicePassport
     * @apiDescription Creates a new device passport.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * @apiParamExample {json} Example Request:
     * {
     *   "deviceId": "aG12Jca"
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        DevicePassport db = deviceManager.createDevicePassport(ctx.getHubContext(), json.getString(JSONAttributes.DEVICE_ID));

        if (db != null) {
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("Device ID is already in use");
        }
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/devicePassports Delete device passports
     * @apiVersion 0.7.0
     * @apiName DeleteDevicePassports
     * @apiDescription Delete all device passports.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        for (DevicePassport db : deviceManager.getDevicePassports(ctx.getHubContext())) {
            deviceManager.deleteDevicePassport(ctx.getHubContext(), db.getId());
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

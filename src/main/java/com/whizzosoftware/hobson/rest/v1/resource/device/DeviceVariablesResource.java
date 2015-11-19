/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.dto.DTOBuildContext;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.HobsonVariableCollection;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaVariableProxyProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A REST resource that retrieves device variable information.
 *
 * @author Dan Noguerol
 */
public class DeviceVariablesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/variables";

    @Inject
    Authorizer authorizer;
    @Inject
    VariableManager variableManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/variables Get all device variables
     * @apiVersion 0.1.3
     * @apiName GetAllDeviceVariables
     * @apiDescription Retrieves a summary list of all variables for a specific device.
     * @apiGroup Variables
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/9/variables",
     *   "numberOfItems": 1,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/9/variables/on",
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        ItemListDTO results = new ItemListDTO(idProvider.createDeviceVariablesId(dctx));

        HobsonVariableCollection c = variableManager.getDeviceVariables(dctx);
        if (c != null) {
            Collection<HobsonVariable> variables = c.getCollection();
            boolean showDetails = expansions.has(JSONAttributes.ITEM);
            expansions.pushContext(JSONAttributes.ITEM);
            DTOBuildContext dbc = new DTOBuildContext.Builder().addProxyValueProvider(new MediaVariableProxyProvider(ctx)).build();
            for (HobsonVariable v : variables) {
                HobsonVariableDTO dto = new HobsonVariableDTO.Builder(
                    dbc,
                    idProvider.createDeviceVariableId(dctx, v.getName()),
                    v,
                    showDetails
                ).build();
                results.add(dto);
            }
            expansions.popContext();
            return new JsonRepresentation(results.toJSON());
        } else {
            throw new HobsonNotFoundException("Unable to find variables for device");
        }
    }

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/variables Set device variables
     * @apiVersion 0.5.0
     * @apiName SetDeviceVariables
     * @apiDescription Updates the values of multiple device variables.
     * @apiGroup Variables
     * @apiExample Example Request:
     * {
     *   "values": {
     *     "on": true,
     *     "level", 100
     *   }
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));

        variableManager.setDeviceVariables(dctx, createDeviceVariableValues(JSONHelper.createJSONFromRepresentation(entity)));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    private Map<String,Object> createDeviceVariableValues(JSONObject json) {
        try {
            Map<String,Object> map = new HashMap<>();
            JSONObject values = json.getJSONObject("values");
            for (Object o : values.keySet()) {
                String key = (String)o;
                map.put(key, values.get(key));
            }
            return map;
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }
}

/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.variable;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.variable.GlobalVariable;
import com.whizzosoftware.hobson.api.variable.VariableMask;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class GlobalVariablesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/globalVariables";

    @Inject
    HubManager hubManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/globalVariables Get all global variables
     * @apiVersion 0.1.3
     * @apiName GetAllGlobalVariables
     * @apiDescription Retrieves a list of all global variables (i.e. those not associated with a particular device).
     * @apiGroup Variables
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/globalVariables/sunset"
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/globalVariables/sunrise"
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        ItemListDTO dto = new ItemListDTO(idProvider.createGlobalVariablesId(ctx.getHubContext()));
        for (GlobalVariable v : hubManager.getGlobalVariables(ctx.getHubContext())) {
            HobsonVariableDTO.Builder builder = new HobsonVariableDTO.Builder(idProvider.createGlobalVariableId(v.getDescription().getContext()));
            if (expansions.has("item")) {
                builder.name(v.getDescription().getName())
                    .mask(VariableMask.READ_ONLY)
                    .lastUpdate(v.getLastUpdate())
                    .value(v.getValue());
            }
            dto.add(builder.build());
        }

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }
}

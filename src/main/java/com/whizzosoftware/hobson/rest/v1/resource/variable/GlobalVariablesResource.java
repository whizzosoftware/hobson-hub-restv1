/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.variable;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class GlobalVariablesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/globalVariables";

    @Inject
    VariableManager variableManager;
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

        ItemListDTO results = new ItemListDTO(idProvider.createGlobalVariablesId(ctx.getHubContext()));
        for (HobsonVariable v : variableManager.getGlobalVariables(ctx.getHubContext())) {
            HobsonVariableDTO.Builder builder = new HobsonVariableDTO.Builder(idProvider.createGlobalVariableId(ctx.getHubContext(), v.getName()));
            if (expansions.has("item")) {
                builder.name(v.getName())
                    .mask(v.getMask())
                    .lastUpdate(v.getLastUpdate())
                    .value(v.getValue());
            }
            results.add(builder.build());
        }

        return new JsonRepresentation(results.toJSON());
    }
}

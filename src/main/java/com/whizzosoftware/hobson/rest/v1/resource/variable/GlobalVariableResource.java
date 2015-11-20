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
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class GlobalVariableResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/globalVariables/{name}";

    @Inject
    VariableManager variableManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/globalVariables/:name Get global variable
     * @apiVersion 0.1.7
     * @apiName GetGlobalVariable
     * @apiDescription Retrieves a global variable (i.e. a variable not associated with a particular device).
     * @apiGroup Variables
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/variables/tempF",
     *   "value": 82.4,
     *   "mask":"READ_ONLY",
     *   "lastUpdated": 199231313,
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        String varName = getAttribute("name");
        HobsonVariable v = variableManager.getGlobalVariable(ctx.getHubContext(), varName);
        return new JsonRepresentation(
            new HobsonVariableDTO.Builder(idProvider.createGlobalVariableId(ctx.getHubContext(), varName))
                .name(v.getName())
                .mask(v.getMask())
                .lastUpdate(v.getLastUpdate())
                .value(v.getValue())
                .build()
                .toJSON()
        );
    }
}

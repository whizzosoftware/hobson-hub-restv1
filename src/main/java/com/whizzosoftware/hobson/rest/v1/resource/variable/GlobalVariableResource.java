/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.variable;

import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class GlobalVariableResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/globalVariables/{name}";

    @Inject
    VariableManager variableManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/globalVariables/:name Get global variable
     * @apiVersion 0.1.7
     * @apiName GetGlobalVariable
     * @apiDescription Retrieves a global variable (i.e. a variable not associated with a particular device).
     * @apiGroup Variables
     * @apiSuccessExample {json} Success Response:
     * {
     *   "value": 82.4,
     *   "lastUpdated": 199231313,
     *   "links": {
     *     "self": "/api/v1/users/local/hubs/local/variables/tempF"
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(JSONMarshaller.createGlobalVariableJSON(ctx, variableManager.getGlobalVariable(ctx.getUserId(), ctx.getHubId(), getAttribute("name"))));
    }
}

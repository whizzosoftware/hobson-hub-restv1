/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.variable;

import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class GlobalVariablesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/globalVariables";
    public static final String REL = "globalVariables";

    @Inject
    Authorizer authorizer;
    @Inject
    VariableManager variableManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/globalVariables Get all global variables
     * @apiVersion 0.1.3
     * @apiName GetAllGlobalVariables
     * @apiDescription Retrieves a list of all global variables (i.e. those not associated with a particular device).
     * @apiGroup Variables
     * @apiSuccessExample {json} Success Response:
     * {
     *   "tempF": {
     *     "value": 82.4,
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/variables/tempF"
     *     }
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        JSONObject results = new JSONObject();
        for (HobsonVariable v : variableManager.getGlobalVariables(ctx.getUserId(), ctx.getHubId())) {
            results.put(
                v.getName(),
                linkHelper.addGlobalVariableLinks(
                    ctx,
                    JSONSerializationHelper.createGlobalVariableJSON(v),
                    v.getName()
                )
            );
        }
        return new JsonRepresentation(results);
    }
}

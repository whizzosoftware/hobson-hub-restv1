/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.action;

import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.action.HobsonAction;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource that returns a summary of all actions.
 *
 * @author Dan Noguerol
 */
public class ActionsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/actions";
    public static final String REL = "actions";

    @Inject
    Authorizer authorizer;
    @Inject
    ActionManager actionManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/actions Get all actions
     * @apiVersion 0.1.3
     * @apiName GetAllActions
     * @apiDescription Retrieves a summary list of all available actions (regardless of plugin).
     * @apiGroup Actions
     *
     * @apiSuccessExample {json} Success Response:
     * {
     *   "sendEmail": {
     *     "name": "Send E-mail",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-api/actions/sendEmail"
     *     }
     *   },
     *   "log": {
     *     "name": "Log Message",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-api/actions/log"
     *     }
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        JSONObject results = new JSONObject();
        for (HobsonAction action : actionManager.getAllActions(ctx.getHubContext())) {
            results.put(
                action.getContext().getActionId(),
                JSONSerializationHelper.createActionJSON(
                    action,
                    false
                )
            );
        }
        return new JsonRepresentation(results);
    }
}

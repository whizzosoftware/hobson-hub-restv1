/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.action;

import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource that returns details of a specific action.
 *
 * @author Dan Noguerol
 */
public class ActionResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/actions/{actionId}";

    @Inject
    ActionManager actionManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/actions/:actionId Get action details
     * @apiVersion 0.1.3
     * @apiName GetActionDetails
     * @apiDescription Retrieves details of a specific action.
     * @apiGroup Actions
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": "Log Message",
     *   "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-api",
     *   "metaOrder": [
     *      "message"
     *   ],
     *   "meta": {
     *     "message": {
     *       "name": "Message",
     *       "description": "The message added to the log file",
     *       "type": "STRING"
     *     }
     *   },
     *   "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-actions/actions/log"
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(JSONMarshaller.createActionJSON(ctx, actionManager.getAction(ctx.getUserId(), ctx.getHubId(), getAttribute("pluginId"), getAttribute("actionId")), true));
    }
}

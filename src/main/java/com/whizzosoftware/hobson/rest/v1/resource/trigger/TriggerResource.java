/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.trigger;

import com.whizzosoftware.hobson.api.trigger.TriggerManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for managing a particular trigger.
 *
 * @author Dan Noguerol
 */
public class TriggerResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/triggers/{providerId}/{triggerId}";

    @Inject
    TriggerManager triggerManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/triggers/:providerId/:triggerId Get trigger details
     * @apiVersion 0.1.3
     * @apiName GetTrigger
     * @apiDescription Retrieves details about a specific trigger.
     * @apiGroup Triggers
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "My Trigger",
     *     "type": "EVENT",
     *     "provider": "com.whizzosoftware.hobson.hub-rules",
     *     "conditions": [{
     *       "leftTerm": "foo",
     *       "op": "eq",
     *       "rightTerm": "bar"
     *     }],
     *     "actions": [{
     *       "pluginId": "com.whizzosoftware.hobson.hub-actions",
     *       "actionId": "log",
     *       "name": "My Action 1",
     *       "properties": {
     *         "message": "Sample log entry"
     *       }
     *     }],
     *     "links": {
     *       "self": "/api/triggers/com.whizzosoftware.hobson.server-rules/efc02d7a-d0e0-46fb-9cc3-2ca70a66dc05"
     *     },
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(JSONMarshaller.createTriggerJSON(ctx, triggerManager.getTrigger(ctx.getUserId(), ctx.getHubId(), getAttribute("providerId"), getAttribute("triggerId")), true, true));
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/triggers/:providerId/:triggerId Delete trigger
     * @apiVersion 0.1.3
     * @apiName DeleteTrigger
     * @apiDescription Deletes a specific trigger.
     * @apiGroup Triggers
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        triggerManager.deleteTrigger(ctx.getUserId(), ctx.getHubId(), getAttribute("providerId"), getAttribute("triggerId"));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

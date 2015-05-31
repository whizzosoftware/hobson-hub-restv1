/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.activity;

import com.whizzosoftware.hobson.api.activity.ActivityLogEntry;
import com.whizzosoftware.hobson.api.activity.ActivityLogManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import org.json.JSONArray;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource that manages a the hub activity log.
 *
 * @author Dan Noguerol
 */
public class ActivityLogResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/activityLog";
    public static final String REL = "activities";

    @Inject
    Authorizer authorizer;
    @Inject
    ActivityLogManager activityManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/activityLog Get activity log
     * @apiVersion 0.1.3
     * @apiName GetActivityLog
     * @apiDescription Retrieves the most recent entries from the hub activity log.
     * @apiGroup Activities
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "timestamp": 1234,
     *     "name": "Thermostat temperature changed to 70",
     *   },
     *   {
     *     "timestamp": 1234,
     *     "name": "Light has turned on",
     *   }
     * ]
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        JSONArray results = new JSONArray();

        for (ActivityLogEntry event : activityManager.getActivityLog(25)) {
            results.put(JSONSerializationHelper.createActivityEventJSON(event));
        }

        return new JsonRepresentation(results);
    }
}
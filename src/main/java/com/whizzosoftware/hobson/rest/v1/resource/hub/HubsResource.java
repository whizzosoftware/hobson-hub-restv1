/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.hub.HobsonHub;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Template;

import javax.inject.Inject;
import java.util.Collections;

public class HubsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs";

    @Inject
    HubManager hubManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs Get Hubs
     * @apiVersion 0.5.0
     * @apiName GetHubs
     * @apiDescription Retrieves the list Hubs associated with the user.
     * @apiGroup Hub
     * @apiSuccessExample Success Response:
     * [
     *   {
     *     "name": "Unnamed",
     *     "links": {
     *       "self": "/api/v1/hubs/local"
     *     }
     *   }
     * ]
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        JSONArray results = new JSONArray();
        for (HobsonHub hub : hubManager.getHubs(ctx.getUserId())) {
            results.put(
                linkHelper.addHubSummaryLinks(
                    ctx,
                    JSONSerializationHelper.createHubSummaryJSON(hub),
                    hub.getContext().getHubId()
                )
            );
        }

        return new JsonRepresentation(results);
    }

    /**
     * @api {get} /api/v1/users/:userId/hubs Create Hub
     * @apiVersion 0.5.0
     * @apiName CreateHub
     * @apiDescription Creates a new Hub associated with the user.
     * @apiGroup Hub
     * @apiExample Example Request:
     * {
     *   "name": "My New Hub"
     * }
     * @apiSuccessExample Success Response:
     * {
     *   "name": "My New Hub",
     *   "links": {
     *     "self": "/api/v1/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa"
     *   }
     * }
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
        HobsonHub hub = hubManager.getRegistrar().addHub(ctx.getHubContext().getUserId(), json.getString("name"));
        String hubId = hub.getContext().getHubId();
        getResponse().setStatus(Status.SUCCESS_CREATED);
        getResponse().setLocationRef(ctx.getApiRoot() + new Template(HubResource.PATH).format(Collections.singletonMap("hubId", hubId)));
        return new JsonRepresentation(
            linkHelper.addHubSummaryLinks(
                ctx,
                JSONSerializationHelper.createHubSummaryJSON(hub),
                hubId
            )
        );
    }
}
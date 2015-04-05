/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.presence.PresenceEntity;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for adding and retrieving presence entities.
 *
 * @author Dan Noguerol
 */
public class PresenceEntitiesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/presence/entities";
    public static final String REL = "presenceEntities";

    @Inject
    Authorizer authorizer;
    @Inject
    PresenceManager presenceManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/presence/entities Get presence entities
     * @apiVersion 0.1.3
     * @apiName GetAllPresenceEntities
     * @apiDescription Retrieves a summary list of all presence entities.
     * @apiGroup Presence
     *
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "John's Mobile Phone",
     *     "location": "home"
     *     "links": {
     *         "self": "/api/v1/users/local/hubs/local/presence/entities/beef-cafe-beeeef-cafe
     *     }
     *   },
     *   {
     *     "name": "Jane's Car",
     *     "location": "office"
     *     "links": {
     *         "self": "/api/v1/users/local/hubs/local/presence/entities/cafe-beef-cafe-beeeef
     *     }
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        JSONArray results = new JSONArray();
        for (PresenceEntity entity : presenceManager.getAllEntities(ctx.getHubContext())) {
            results.put(
                linkHelper.addPresenceEntityLinks(
                    ctx,
                    JSONSerializationHelper.createPresenceEntityJSON(
                        entity,
                        false
                    ),
                    entity.getContext().getEntityId()
                )
            );
        }
        return new JsonRepresentation(results);
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/presence/entities Add presence entity
     * @apiVersion 0.1.3
     * @apiName AddPresenceEntity
     * @apiDescription Adds a new presence entity.
     * @apiGroup Presence
     *
     * @apiExample {json} Example Request:
     * {
     *   "name": "Jane's Car",
     *   "location": null
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
        presenceManager.addEntity(new PresenceEntity(ctx.getHubContext(), json.getString("name"), json.getString("location")));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

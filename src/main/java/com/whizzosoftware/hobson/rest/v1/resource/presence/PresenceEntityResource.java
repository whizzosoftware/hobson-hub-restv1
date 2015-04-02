/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for retrieving a list of presence entities.
 *
 * @author Dan Noguerol
 */
public class PresenceEntityResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/presence/entities/{entityId}";

    @Inject
    Authorizer authorizer;
    @Inject
    PresenceManager presenceManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/presence/entities/:entityId Get presence entity
     * @apiVersion 0.1.3
     * @apiName GetPresenceEntity
     * @apiDescription Retrieves details of a presence entity.
     * @apiGroup Presence
     *
     * @apiSuccessExample Success Response:
     * {
     *   "name": "John's Mobile Phone",
     *   "location": "home",
     *   "lastUpdate": 1416007036
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        String entityId = getAttribute("entityId");
        return new JsonRepresentation(
            linkHelper.addPresenceEntityLinks(
                ctx,
                JSONSerializationHelper.createPresenceEntityJSON(
                    presenceManager.getEntity(ctx.getUserId(), ctx.getHubId(), entityId),
                    true
                ),
                entityId
            )
        );
    }
}

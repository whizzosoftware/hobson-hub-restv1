/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.presence.PresenceLocation;
import com.whizzosoftware.hobson.api.presence.PresenceLocationContext;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class PresenceLocationResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/presence/locations/{locationId}";

    @Inject
    Authorizer authorizer;
    @Inject
    PresenceManager presenceManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/presence/locations/:locationId Get location
     * @apiVersion 0.7.0
     * @apiName GetLocation
     * @apiDescription Retrieves details about a specific location.
     * @apiGroup Presence
     *
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/presence/locations/beef-cafe-beeeef-cafe",
     *   "name": "My Home",
     *   "latitude": 0.000,
     *   "longitude": 0.000,
     *   "radius": 100
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        PresenceLocation location = presenceManager.getLocation(PresenceLocationContext.create(ctx.getHubContext(), getAttribute("locationId")));
        return new JsonRepresentation(DTOMapper.mapPresenceLocation(location, true, linkProvider).toJSON());
    }
}

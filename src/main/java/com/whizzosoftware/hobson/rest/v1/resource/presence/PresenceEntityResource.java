/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.event.EventManager;
import com.whizzosoftware.hobson.api.event.PresenceUpdateRequestEvent;
import com.whizzosoftware.hobson.api.presence.PresenceEntity;
import com.whizzosoftware.hobson.api.presence.PresenceEntityContext;
import com.whizzosoftware.hobson.api.presence.PresenceLocation;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.dto.presence.PresenceLocationDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
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
    EventManager eventManager;
    @Inject
    LinkProvider linkProvider;

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
     *   "location": {
     *       "@id": "/api/v1/users/local/hubs/local/presence/locations/beef-cafe-beeeef-cafe"
     *   },
     *   "lastUpdate": 1416007036
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        PresenceEntityContext pctx = PresenceEntityContext.create(ctx.getHubContext(), getAttribute("entityId"));
        PresenceEntity entity = presenceManager.getEntity(pctx);

        return new JsonRepresentation(DTOMapper.mapPresenceEntity(entity, presenceManager, new ExpansionFields("item"), linkProvider));
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/presence/entities/:entityId Update presence entity
     * @apiVersion 0.1.3
     * @apiName UpdatePresenceEntity
     * @apiDescription Updates a presence entity.
     * @apiGroup Presence
     *
     * @apiExample {json} Example known location request:
     * {
     *   "location": {
     *      "@id": "/api/v1/users/local/hubs/local/presence/locations/beef-cafe-beeeef-cafe"
     *   }
     * }
     * @apiExample {json} Example unknown location request:
     * {
     *   "location": {}
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        PresenceEntityContext pec = PresenceEntityContext.create(ctx.getHubContext(), getAttribute("entityId"));
        PresenceLocationDTO dto = new PresenceLocationDTO.Builder(json.getJSONObject("location")).build();

        PresenceLocation loc = DTOMapper.mapPresenceLocationDTO(dto);
        eventManager.postEvent(ctx.getHubContext(), new PresenceUpdateRequestEvent(System.currentTimeMillis(), pec, loc != null ? loc.getContext() : null));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

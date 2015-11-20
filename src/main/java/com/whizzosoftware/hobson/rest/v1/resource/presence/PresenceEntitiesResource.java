/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.presence.PresenceEntity;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.presence.PresenceEntityDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONObject;
import org.restlet.data.MediaType;
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

    @Inject
    PresenceManager presenceManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/presence/entities Get presence entities
     * @apiVersion 0.1.3
     * @apiName GetAllPresenceEntities
     * @apiDescription Retrieves a list of all presence entities.
     * @apiGroup Presence
     *
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/presence/entities/beef-cafe-beeeef-cafe",
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/presence/entities/cafe-beef-cafe-beeeef",
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        ItemListDTO results = new ItemListDTO(idProvider.createPresenceEntitiesId(ctx.getHubContext()), true);
        boolean showDetails = expansions.has(JSONAttributes.ITEM);
        expansions.pushContext(JSONAttributes.ITEM);
        for (PresenceEntity entity : presenceManager.getAllPresenceEntities(ctx.getHubContext())) {
            results.add(new PresenceEntityDTO.Builder(entity, presenceManager, showDetails, expansions, idProvider).build());
        }
        expansions.popContext();

        JsonRepresentation jr = new JsonRepresentation(results.toJSON());
        jr.setMediaType(new MediaType(results.getJSONMediaType()));
        return jr;
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
     *   "name": "Jane's Car"
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
        if (json.has("name") && json.getString("name").trim().length() > 0) {
            presenceManager.addPresenceEntity(ctx.getHubContext(), json.getString("name"));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("A name is required");
        }
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/presence/entities Deletes all presence entities
     * @apiVersion 0.7.0
     * @apiName DeletePresenceEntities
     * @apiDescription Deletes all presence entities.
     * @apiGroup Presence
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        for (PresenceEntity entity : presenceManager.getAllPresenceEntities(ctx.getHubContext())) {
            presenceManager.deletePresenceEntity(entity.getContext());
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.presence.PresenceLocation;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.presence.PresenceLocationDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.dto.ExpansionFields;
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

public class PresenceLocationsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/presence/locations";

    @Inject
    PresenceManager presenceManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/presence/locations Get presence locations
     * @apiVersion 0.7.0
     * @apiName GetAllPresenceLocations
     * @apiDescription Retrieves a list of all locations.
     * @apiGroup Presence
     *
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/presence/locations/beef-cafe-beeeef-cafe",
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/presence/locations/cafe-beef-cafe-beeeef",
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        ItemListDTO results = new ItemListDTO(idProvider.createPresenceLocationsId(ctx.getHubContext()), true);
        for (PresenceLocation location : presenceManager.getAllPresenceLocations(ctx.getHubContext())) {
            results.add(new PresenceLocationDTO.Builder(location, idProvider, expansions.has(JSONAttributes.ITEM)).build());
        }

        JsonRepresentation jr = new JsonRepresentation(results.toJSON());
        jr.setMediaType(new MediaType(results.getJSONMediaType()));
        return jr;
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/presence/locations Add presence location
     * @apiVersion 0.1.3
     * @apiName AddPresenceLocation
     * @apiDescription Adds a new presence location.
     * @apiGroup Presence
     *
     * @apiExample {json} Example Request:
     * {
     *   "name": "Jane's Car",
     *   "latitude": 0.000,
     *   "longitude": 0.000,
     *   "radius": 30
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        Double latitude = null;
        Double longitude = null;
        Double radius = null;
        Integer beaconMajor = null;
        Integer beaconMinor = null;

        if (json.has(JSONAttributes.LATITUDE)) {
            latitude = json.getDouble(JSONAttributes.LATITUDE);
        }
        if (json.has(JSONAttributes.LONGITUDE)) {
            longitude = json.getDouble(JSONAttributes.LONGITUDE);
        }
        if (json.has(JSONAttributes.RADIUS)) {
            radius = json.getDouble(JSONAttributes.RADIUS);
        }
        if (json.has(JSONAttributes.BEACON_MAJOR)) {
            beaconMajor = json.getInt(JSONAttributes.BEACON_MAJOR);
        }
        if (json.has(JSONAttributes.BEACON_MAJOR)) {
            beaconMinor = json.getInt(JSONAttributes.BEACON_MINOR);
        }

        presenceManager.addPresenceLocation(ctx.getHubContext(), json.getString(JSONAttributes.NAME), latitude, longitude, radius, beaconMajor, beaconMinor);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/presence/locations Delete presence locations
     * @apiVersion 0.7.0
     * @apiName DeletePresenceLocations
     * @apiDescription Deletes all presence locations.
     * @apiGroup Presence
     *
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        for (PresenceLocation pl : presenceManager.getAllPresenceLocations(ctx.getHubContext())) {
            presenceManager.deletePresenceLocation(pl.getContext());
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.hub.HobsonHub;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.dto.hub.HobsonHubDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class HubsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs";

    @Inject
    HubManager hubManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs Get Hubs
     * @apiVersion 0.5.0
     * @apiParam (Query Parameters) {String} expand A comma-separated list of fields to expand in the response. Valid field values are "_links".
     * @apiName GetHubs
     * @apiDescription Retrieves the list of Hubs associated with a user.
     * @apiGroup User
     * @apiSuccessExample Success Response:
     * [
     *   {
     *     "name": "Unnamed",
     *     "_links": {
     *       "self": {
     *         "href": "/api/v1/users/local/hubs/local"
     *       }
     *     }
     *   }
     * ]
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        ItemListDTO itemList = new ItemListDTO(linkProvider.createHubsLink(ctx.getUserId()));
        for (HobsonHub hub : hubManager.getHubs(ctx.getUserId())) {
            itemList.add(new HobsonHubDTO.Builder(linkProvider.createHubLink(hub.getContext())).build());
        }

        JsonRepresentation jr = new JsonRepresentation(itemList.toJSON());
        jr.setMediaType(new MediaType(itemList.getMediaType() + "+json"));
        return jr;
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
        HobsonHubDTO dto = new HobsonHubDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        HobsonHub hub = hubManager.addHub(ctx.getHubContext().getUserId(), dto.getName());
        String hubId = hub.getContext().getHubId();
        getResponse().setStatus(Status.SUCCESS_CREATED);
        String hubLink = linkProvider.createHubLink(HubContext.create(ctx.getUserId(), hubId));
        getResponse().setLocationRef(hubLink);
        return new JsonRepresentation(new HobsonHubDTO.Builder(hubLink)
            .name(hub.getName())
            .version(hub.getVersion())
            .build()
            .toJSON()
        );
    }
}

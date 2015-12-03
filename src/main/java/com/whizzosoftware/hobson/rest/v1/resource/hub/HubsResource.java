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
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.hub.HobsonHubDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;

public class HubsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs";

    @Inject
    HubManager hubManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs Get Hubs
     * @apiVersion 0.5.0
     * @apiParam (Query Parameters) {String} expand A comma-separated list of fields to expand in the response. Valid field values are "item".
     * @apiName GetHubs
     * @apiDescription Retrieves the list of Hubs associated with a user.
     * @apiGroup User
     * @apiSuccessExample Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs"
     *   "numberOfItems": 1,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local"
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        ItemListDTO itemList = new ItemListDTO(idProvider.createHubsId(ctx.getUserId()));
        Collection<HobsonHub> hubs = hubManager.getHubs(ctx.getUserId());

        if (hubs != null) {
            boolean showDetails = expansions.has(JSONAttributes.ITEM);
            expansions.pushContext(JSONAttributes.ITEM);

            for (HobsonHub hub : hubs) {
                itemList.add(new HobsonHubDTO.Builder(
                    dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                    hub,
                    showDetails
                ).build());
            }

            expansions.popContext();
        }

        JsonRepresentation jr = new JsonRepresentation(itemList.toJSON());
        jr.setMediaType(new MediaType(itemList.getJSONMediaType()));
        return jr;
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs Create Hub
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
     *   "@id": "/api/v1/users/jdoe/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa",
     *   "actionClasses": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/tasks/actionClasses"},
     *   "conditionClasses": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/tasks/conditionClasses"},
     *   "configuration": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/configuration"},
     *   "configurationClass": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/configurationClass"},
     *   "devices": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/devices"},
     *   "log": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/log"},
     *   "name": "My New Hub",
     *   "localPlugins": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/plugins/local"},
     *   "remotePlugins": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/plugins/remote"},
     *   "tasks": {"@id": "/api/v1/users/local/hubs/722711d0-08cb-4b05-8c3e-180d7d1a67aa/tasks"},
     * }
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        HobsonHubDTO dto = new HobsonHubDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        HobsonHub hub = hubManager.addHub(ctx.getHubContext().getUserId(), dto.getName());
        String hubId = hub.getContext().getHubId();
        getResponse().setStatus(Status.SUCCESS_CREATED);
        String hubLink = idProvider.createHubId(HubContext.create(ctx.getUserId(), hubId));
        getResponse().setLocationRef(hubLink);
        return new JsonRepresentation(
            new HobsonHubDTO.Builder(
                dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                hub,
                false
            ).build().toJSON()
        );
    }
}

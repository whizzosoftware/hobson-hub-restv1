/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class HubConfigurationResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/configuration";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/configuration Get Hub configuration
     * @apiVersion 0.5.0
     * @apiName GetHubConfiguration
     * @apiDescription Retrieves current configuration settings for a Hub.
     * @apiGroup Hub
     * @apiSuccess {Object} cclass The configuration class associated with the Hub
     * @apiSuccess {Object} values The configuration values
     * @apiSuccessExample Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/configuration",
     *   "cclass": {
     *     "@id": "/api/v1/users/local/hubs/local/configurationClass"
     *   },
     *   "values": {
     *     "name": "Test Hub"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        PropertyContainer pc = hubManager.getConfiguration(ctx.getHubContext());
        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(
            pc,
            new PropertyContainerClassProvider() {
                @Override
                public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                    return hubManager.getConfigurationClass(ctx.getHubContext());
                }
            },
            PropertyContainerClassType.HUB_CONFIG,
            true,
            expansions,
            idProvider
        ).build();

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getJSONMediaType()));
        return jr;
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/configuration Set Hub configuration
     * @apiVersion 0.5.0
     * @apiName SetHubConfiguration
     * @apiDescription Sets the current configuration settings for a Hub.
     * @apiGroup Hub
     * @apiSuccessExample Success Response:
     * {
     *   "cclass": {
     *     "@id": "/api/v1/users/local/hubs/local/configurationClass"
     *   },
     *   "values": {
     *     "name": "Demo Hub"
     *   }
     * }
     *
     */
    @Override
    protected Representation put(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        PropertyContainer pc = DTOMapper.mapPropertyContainerDTO(dto, null, idProvider);
        hubManager.setConfiguration(ctx.getHubContext(), pc);

        setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    @Override
    protected Representation delete() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        hubManager.deleteConfiguration(ctx.getHubContext());

        setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

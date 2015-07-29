/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.plugin.EnableRemoteRepositoryDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A resource for enabling/disabling remote plugin repositories.
 *
 * @author Dan Noguerol
 */
public class HubEnableRemoteRepositoryResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/enableRemoteRepository";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/enableRemoteRepository Enable/disable remote repository
     * @apiVersion 0.5.0
     * @apiName EnableRemoteRepository
     * @apiDescription Enables or disables a remote repository specified by a URL.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "url": "http://your-repo-url-here",
     *   "enabled": true
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        if (pluginManager != null) {
            EnableRemoteRepositoryDTO dto = new EnableRemoteRepositoryDTO(JSONHelper.createJSONFromRepresentation(entity));
            pluginManager.enableRemoteRepository(dto.getUrl(), dto.isEnabled());
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonRuntimeException("No plugin manager found");
        }
    }
}

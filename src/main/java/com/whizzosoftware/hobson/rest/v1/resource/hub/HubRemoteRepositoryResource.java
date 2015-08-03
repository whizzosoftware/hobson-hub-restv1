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
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HubRemoteRepositoryResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/repositories/{repositoryId}";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/repositories Delete remote repository
     * @apiVersion 0.5.0
     * @apiName DeleteRemoteRepository
     * @apiDescription Removes a remote repository from the Hub's configuration.
     * @apiGroup Hub
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        if (pluginManager != null) {
            try {
                pluginManager.removeRemoteRepository(URLDecoder.decode(getAttribute("repositoryId"), "UTF-8"));
                getResponse().setStatus(Status.SUCCESS_ACCEPTED);
                return new EmptyRepresentation();
            } catch (UnsupportedEncodingException e) {
                throw new HobsonRuntimeException("URL decoding failed", e);
            }
        } else {
            throw new HobsonRuntimeException("No plugin manager found");
        }
    }
}

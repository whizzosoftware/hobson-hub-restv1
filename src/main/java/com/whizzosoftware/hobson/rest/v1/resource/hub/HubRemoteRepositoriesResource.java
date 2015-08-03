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
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.hub.RepositoryDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;

public class HubRemoteRepositoriesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/repositories";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/repositories Get remote repositories
     * @apiVersion 0.5.0
     * @apiName GetRemoteRepositories
     * @apiDescription Retrieves a URI list of remote repositories.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "numberOfItems": "1",
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "uri": "http://your-uri-here",
     *       }
     *     }
     *   ]
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        if (pluginManager != null) {
            ItemListDTO results = new ItemListDTO(linkProvider.createRepositoriesLink(ctx.getHubContext()));
            Collection<String> repositories = pluginManager.getRemoteRepositories();
            if (repositories != null) {
                for (String uri : repositories) {
                    results.add(new RepositoryDTO(linkProvider.createRepositoryLink(ctx.getHubContext(), uri), uri));
                }
            }
            return new JsonRepresentation(results.toJSON());
        } else {
            throw new HobsonRuntimeException("No plugin manager found");
        }
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/repositories Add remote repository
     * @apiVersion 0.5.0
     * @apiName EnableRemoteRepository
     * @apiDescription Enables or disables a remote repository specified by a URL.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "url": "http://your-repo-url-here",
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        if (pluginManager != null) {
            RepositoryDTO dto = new RepositoryDTO(JSONHelper.createJSONFromRepresentation(entity));
            pluginManager.addRemoteRepository(dto.getUri());
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonRuntimeException("No plugin manager found");
        }
    }
}

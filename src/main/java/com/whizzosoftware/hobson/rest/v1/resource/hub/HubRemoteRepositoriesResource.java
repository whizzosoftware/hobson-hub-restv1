/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.hub.RepositoryDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.MediaType;
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
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/repositories Get remote repositories
     * @apiVersion 0.5.0
     * @apiName GetRemoteRepositories
     * @apiDescription Retrieves a URI list of remote repositories.
     * @apiGroup Hub
     * @apiSuccessExample {json} Success Response:
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
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        if (pluginManager != null) {
            boolean showDetails = expansions.has(JSONAttributes.ITEM);

            ItemListDTO results = new ItemListDTO(idProvider.createRepositoriesId(ctx.getHubContext()));

            Collection<String> repositories = pluginManager.getRemoteRepositories();
            if (repositories != null) {
                for (String uri : repositories) {
                    results.add(new RepositoryDTO(idProvider.createRepositoryId(ctx.getHubContext(), uri), showDetails ? uri : null));
                }
            }

            JsonRepresentation jr = new JsonRepresentation(results.toJSON());
            jr.setMediaType(new MediaType(results.getJSONMediaType()));
            return jr;
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
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
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

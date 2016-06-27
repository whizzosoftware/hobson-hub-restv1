/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.hub.RepositoryDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class HubRemoteRepositoryResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/repositories/{repositoryId}";

    @Inject
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/repositories/:repositoryId Get remote repository details
     * @apiVersion 0.7.0
     * @apiName GetRemoteRepository
     * @apiDescription Retrieves the details of a remote repository.
     * @apiGroup Hub
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/repositories/aqsfeeqedfvewfew",
     *   "uri": "http://your-uri-here",
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        try {
            String repositoryId = getAttribute("repositoryId");
            String repositoryUrl = null;
            for (String url : pluginManager.getRemoteRepositories()) {
                if (repositoryId.equals(URLEncoder.encode(url, "UTF8"))) {
                    repositoryUrl = url;
                    break;
                }
            }

            if (repositoryUrl != null) {
                RepositoryDTO dto = new RepositoryDTO(idProvider.createRepositoryId(ctx.getHubContext(), repositoryUrl), repositoryUrl);
                JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
                jr.setMediaType(new MediaType(dto.getJSONMediaType()));
                return jr;
            } else {
                throw new HobsonNotFoundException("No repository found");
            }
        } catch (UnsupportedEncodingException e) {
            throw new HobsonRuntimeException("UTF8 encoding is not supported", e);
        }
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/repositories/:repositoryId Delete remote repository
     * @apiVersion 0.5.0
     * @apiName DeleteRemoteRepository
     * @apiDescription Removes a remote repository from the Hub's configuration.
     * @apiGroup Hub
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
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

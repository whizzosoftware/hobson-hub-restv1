/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for reloading a plugin.
 *
 * @author Dan Noguerol
 */
public class PluginReloadResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/reload";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/reload Reload plugin
     * @apiVersion 0.1.6
     * @apiName ReloadPlugin
     * @apiDescription Reload a plugin.
     * @apiGroup Plugin
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        String pluginId = getAttribute("pluginId");

        pluginManager.reloadPlugin(ctx.getUserId(), ctx.getHubId(), pluginId);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

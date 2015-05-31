/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Template;

import javax.inject.Inject;

/**
 * A REST resource for installing an available plugin.
 *
 * @author Dan Noguerol
 */
public class PluginInstallResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/{pluginVersion}/install";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/:pluginVersion/install Install plugin
     * @apiVersion 0.1.6
     * @apiName InstallPlugin
     * @apiDescription Install a plugin from the Hobson plugin repository.
     *
     * The response header Location provides a polling URI to determine when the plugin version has changed,
     * indicating a successful installation.
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     * Location: http://localhost:8080/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/currentVersion
     */
    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());

        String pluginId = getAttribute("pluginId");
        String pluginVersion = getAttribute("pluginVersion");

        pluginManager.installPlugin(PluginContext.create(ctx.getHubContext(), pluginId), pluginVersion);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        getResponse().setLocationRef(ctx.getApiRoot() + new Template(PluginCurrentVersionResource.PATH).format(linkHelper.createSingleEntryMap(ctx, "pluginId", pluginId)));

        return new EmptyRepresentation();
    }
}

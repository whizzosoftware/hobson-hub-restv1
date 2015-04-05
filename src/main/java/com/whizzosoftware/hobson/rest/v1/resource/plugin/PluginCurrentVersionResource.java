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
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource for obtaining the current local version of a plugin.
 *
 * @author Dan Noguerol
 */
public class PluginCurrentVersionResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/currentVersion";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/currentVersion Get plugin current version
     * @apiVersion 0.1.6
     * @apiName GetPluginCurrentVersion
     * @apiDescription Retrieves the current version of a specific plugin
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response (plugin installed):
     * {
     *   "currentVersion": "0.0.2.SNAPSHOT"
     * }
     * @apiSuccessExample {json} Success Response (plugin not installed):
     * {
     *   "currentVersion": "0.0.0"
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        String pluginId = getAttribute("pluginId");
        return new JsonRepresentation(JSONSerializationHelper.createCurrentVersionJSON(pluginManager.getPluginCurrentVersion(PluginContext.create(ctx.getHubContext(), pluginId))));
    }
}

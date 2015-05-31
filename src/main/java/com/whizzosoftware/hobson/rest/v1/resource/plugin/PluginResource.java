/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginList;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource for obtaining plugin information.
 *
 * @author Dan Noguerol
 */
public class PluginResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId Get plugin details
     * @apiVersion 0.1.6
     * @apiName GetPluginDetails
     * @apiDescription Retrieves details about a plugin
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": "Hobson RadioRa Plugin",
     *   "type": "PLUGIN",
     *   "status": {
     *     "status": "RUNNING"
     *   },
     *   "currentVersion": "0.0.2.SNAPSHOT",
     *   "links": {
     *     "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora",
     *     "configuration": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/configuration",
     *     "reload": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/reload"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        String pluginId = getAttribute("pluginId");

        // TODO: this whole thing can probably be made way more efficient
        // generate a plugin list
        PluginList bl = pluginManager.getPluginDescriptors(ctx.getHubContext(), true);

        // build a JSON response array
        for (PluginDescriptor pd : bl.getPlugins()) {
            if (pluginId.equals(pd.getId())) {
                return new JsonRepresentation(
                    linkHelper.addPluginDescriptorLinks(
                        ctx,
                        JSONSerializationHelper.createPluginDescriptorJSON(
                            pd,
                            true
                        ),
                        pd,
                        true
                    )
                );
            }
        }

        throw new HobsonNotFoundException("Plugin not found");
    }
}

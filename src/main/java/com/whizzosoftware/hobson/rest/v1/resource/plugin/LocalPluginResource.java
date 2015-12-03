/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class LocalPluginResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/local/{pluginId}";

    @Inject
    PluginManager pluginManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/local/:pluginId Get local plugin details
     * @apiVersion 0.5.0
     * @apiName GetLocalPlugin
     * @apiDescription Retrieves details of a local plugin.
     * @apiGroup Plugin
     * @apiSuccess {Boolean} configurable Indicates whether the plugin has configurable properties.
     * @apiSuccess {Object} configuration The current configuration values for the plugin.
     * @apiSuccess {Object} configurationClass The plugin's configuration class.
     * @apiSuccess {Object} image The image associated with the plugin.
     * @apiSuccess {String} name The plugin name.
     * @apiSuccess {Object} status The current plugin status (comprised of a code and an optional message).
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-radiora",
     *   "configurable": true,
     *   "configuration": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-lutron-radiora/configuration"
     *   },
     *   "configurationClass": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-lutron-radiora/configurationClass"
     *   },
     *   "image": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-lutron-radiora/image"
     *   },
     *   "name": "RadioRa Plugin",
     *   "status": {
     *     "code": "NOT_CONFIGURED",
     *     "message": "Neither serial port nor serial hostname are configured"
     *   },
     *   "version": "0.5.0"
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        HobsonPlugin plugin = pluginManager.getLocalPlugin(pctx);

        HobsonPluginDTO dto = new HobsonPluginDTO.Builder(
            dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
            plugin,
            null,
            null,
            true
        ).build();
        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getJSONMediaType()));
        return jr;
    }
}

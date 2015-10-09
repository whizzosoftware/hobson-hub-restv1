/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.PluginDescriptorAdaptor;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class RemotePluginResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/remote/{pluginId}/{pluginVersion}";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/remote/:pluginId/:pluginVersion Get remote plugin details
     * @apiVersion 0.5.0
     * @apiName GetRemotePlugin
     * @apiDescription Retrieves details of a remote plugin.
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-radiora",
     *   "description": "Provides the ability to control Lutron RadioRA Classic lighting systems.",
     *   "links": {
     *     "install": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-radiora/0.5.0/install"
     *   },
     *   "name": "RadioRa Plugin",
     *   "version": "0.5.0"
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        String version = getQueryValue("pluginVersion");
        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getQueryValue("pluginId"));
        PluginDescriptor pd = pluginManager.getRemotePluginDescriptor(pctx, version);

        HobsonPluginDTO dto = DTOMapper.mapPlugin(new PluginDescriptorAdaptor(pd, null), pd.getDescription(), null, null, true, expansions, true, linkProvider);

        return new JsonRepresentation(dto.toJSON());
    }
}

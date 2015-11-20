/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.*;
import com.whizzosoftware.hobson.dto.DTOBuildContext;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.PluginDescriptorAdaptor;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class LocalPluginsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/local";

    @Inject
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/local Get local plugins
     * @apiVersion 0.5.0
     * @apiName GetLocalPlugins
     * @apiDescription Retrieves all locally installed plugins.
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/local",
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-core"
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-actions"
     *       }
     *     }
     *   ],
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        ItemListDTO results = new ItemListDTO(idProvider.createLocalPluginsId(ctx.getHubContext()));

        boolean itemExpand = expansions.has("item");
        for (PluginDescriptor pd : pluginManager.getLocalPluginDescriptors(ctx.getHubContext())) {
            PluginContext pctx = PluginContext.create(ctx.getHubContext(), pd.getId());
            final HobsonPlugin plugin = pd.isConfigurable() ? pluginManager.getLocalPlugin(pctx) : null;
            results.add(new HobsonPluginDTO.Builder(new DTOBuildContext.Builder().pluginManager(pluginManager).expansionFields(expansions).idProvider(idProvider).build(), new PluginDescriptorAdaptor(pd, plugin), pd.getDescription(), null, itemExpand).build());
        }

        JsonRepresentation jr = new JsonRepresentation(results.toJSON());
        jr.setMediaType(new MediaType(results.getJSONMediaType()));
        return jr;
    }
}

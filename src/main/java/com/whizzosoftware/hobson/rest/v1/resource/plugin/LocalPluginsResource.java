/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.plugin.*;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.PluginDescriptorAdaptor;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class LocalPluginsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/local";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;
    @Inject
    LinkProvider linkProvider;

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
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        ItemListDTO results = new ItemListDTO(linkProvider.createLocalPluginsLink(ctx.getHubContext()));

        boolean itemExpand = expansions.has("item");
        for (PluginDescriptor pd : pluginManager.getLocalPluginDescriptors(ctx.getHubContext())) {
            PluginContext pctx = PluginContext.create(ctx.getHubContext(), pd.getId());
            final HobsonPlugin plugin;
            final PropertyContainerClassProvider pccp;
            final PropertyContainer pluginConfig;

            if (pd.isConfigurable()) {
                plugin = pluginManager.getLocalPlugin(pctx);
                pluginConfig = pluginManager.getLocalPluginConfiguration(pctx);
                pccp = new PropertyContainerClassProvider() {
                    @Override
                    public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                        return plugin.getConfigurationClass();
                    }
                };
            } else {
                pccp = null;
                plugin = null;
                pluginConfig = null;
            }

            HobsonPluginDTO dto = DTOMapper.mapPlugin(new PluginDescriptorAdaptor(pd, plugin), pd.getDescription(), pluginConfig, pccp, itemExpand, expansions, false, linkProvider);

            results.add(dto);
        }

        return new JsonRepresentation(results.toJSON());
    }
}

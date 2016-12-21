/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.HobsonPluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class RemotePluginsResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/remote";

    @Inject
    PluginManager pluginManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/remote Get remote plugins
     * @apiVersion 0.5.0
     * @apiName GetRemotePlugins
     * @apiDescription Retrieves all remotely available plugins.
     * @apiGroup Plugin
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/remote",
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/remote/com.whizzosoftware.hobson.hub.hobson-hub-lutron-radiora"
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/remote/com.whizzosoftware.hobson.hub.hobson-hub-philips-hue"
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        ItemListDTO results = new ItemListDTO(idProvider.createRemotePluginsId(ctx.getHubContext()), true);

        boolean itemExpand = expansions.has("item");
        for (HobsonPluginDescriptor pd : pluginManager.getRemotePlugins(ctx.getHubContext())) {
            HobsonPluginDTO dto = new HobsonPluginDTO.Builder(
                dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                ctx.getHubContext(),
                pd,
                pd.getDescription(),
                pd.getVersion(),
                itemExpand
            ).build();
            if (itemExpand) {
                dto.addLink("install", idProvider.createRemotePluginInstallId(ctx.getHubContext(), pd.getId(), pd.getVersion()));
            }
            results.add(dto);
        }

        JsonRepresentation jr = new JsonRepresentation(results.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), results));
        return jr;
    }
}

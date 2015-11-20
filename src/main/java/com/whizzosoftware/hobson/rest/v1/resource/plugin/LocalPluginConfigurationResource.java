/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource for obtaining plugin configuration information.
 *
 * @author Dan Noguerol
 */
public class LocalPluginConfigurationResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/local/{pluginId}/configuration";

    @Inject
    HubManager hubManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/local/:pluginId/configuration Get local plugin configuration
     * @apiVersion 0.5.0
     * @apiName GetLocalPluginConfiguration
     * @apiDescription Retrieves a local plugin's configuration.
     * @apiGroup Plugin
     * @apiSuccess {Object} cclass The configuration class associated with the configuration
     * @apiSuccess {Object} values The configuration values
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-wunderground/configuration",
     *   "cclass": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-wunderground/configurationClass"
     *   },
     *   "values": {
     *     "pwsId": "KCOFOO",
     *     "device": {
     *       "@id": "/api/v1/users/local/hubs/local/plugins/plugin1/devices/device1"
     *     }
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        String pluginId = getAttribute("pluginId");
        PluginContext pctx = PluginContext.create(ctx.getHubContext(), pluginId);
        final HobsonPlugin plugin = pluginManager.getLocalPlugin(pctx);
        PropertyContainer config = pluginManager.getLocalPluginConfiguration(pctx);

        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(
            config,
            new PropertyContainerClassProvider() {
                @Override
                public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                    return plugin.getConfigurationClass();
                }
            },
            PropertyContainerClassType.PLUGIN_CONFIG,
            true,
            expansions,
            idProvider
        ).build();

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getJSONMediaType()));
        return jr;
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/local/:pluginId/configuration Set local plugin configuration
     * @apiVersion 0.1.6
     * @apiName SetLocalPluginConfiguration
     * @apiDescription Sets a local plugin's configuration.
     * @apiGroup Plugin
     * @apiExample {json} Example Request:
     * {
     *   "cclass": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-wunderground/configurationClass"
     *   },
     *   "values": {
     *     "pwsId": "KCOFOO",
     *     "device": {
 *           "@id": "/api/v1/users/local/hubs/local/plugins/plugin1/devices/device1"
     *     }
     *   }
     * }
     * @apiSuccessExample {json} Success Response
     * HTTP/1.1 202 Accepted
     * Location: http://localhost:8080/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/configuration
     */
    @Override
    protected Representation put(Representation entity) throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        PluginContext pc = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        final HobsonPlugin plugin = pluginManager.getLocalPlugin(pc);

        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return plugin.getConfigurationClass();
            }
        };

        pluginManager.setLocalPluginConfiguration(
            pc,
            DTOMapper.mapPropertyContainerDTO(dto, pccp, idProvider)
        );

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

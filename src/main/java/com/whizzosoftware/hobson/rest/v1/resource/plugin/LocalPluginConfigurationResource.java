/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
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
    Authorizer authorizer;
    @Inject
    HubManager hubManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    LinkProvider linkProvider;

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
        String pluginId = getAttribute("pluginId");
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());

        PluginContext pctx = PluginContext.create(ctx.getHubContext(), pluginId);
        final HobsonPlugin plugin = pluginManager.getLocalPlugin(pctx);
        PropertyContainer config = pluginManager.getLocalPluginConfiguration(pctx);

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return plugin.getConfigurationClass();
            }
        };

        return new JsonRepresentation(DTOMapper.mapPropertyContainer(config, pccp, false, linkProvider).toJSON());
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
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());

        PluginContext pc = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        final HobsonPlugin plugin = pluginManager.getLocalPlugin(pc);

        PropertyContainerDTO dto = new PropertyContainerDTO(JSONHelper.createJSONFromRepresentation(entity));

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return plugin.getConfigurationClass();
            }
        };

        pluginManager.setLocalPluginConfiguration(pc, DTOMapper.mapPropertyContainerDTO(dto, pccp, linkProvider));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

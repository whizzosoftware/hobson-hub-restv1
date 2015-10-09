/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class LocalPluginConfigurationClassResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/local/{pluginId}/configurationClass";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/local/:pluginId/configurationClass Get local plugin configuration class
     * @apiVersion 0.5.0
     * @apiName GetLocalPluginConfigurationClass
     * @apiDescription Retrieves configuration class of a local plugin.
     * @apiGroup Plugin
     * @apiSuccess {String} supportedProperties A list of configuration properties supported by the plugin.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-radiora/configurationClass",
     *   "supportedProperties": [
     *     {
     *       "@id":"serial.port",
     *       "description": "The serial port that the Lutron RA-RS232 controller is connected to (should not be used with Serial Hostname)",
     *       "name":"Serial Port",
     *       "type":"STRING"
     *     },
     *     {
     *       "@id": "serial.hostname",
     *       "description": "The hostname of the GlobalCache device that the Lutron RA-RS232 controller is connected to (should not be used with Serial Port)",
     *       "name": "Serial Hostname",
     *       "type": "STRING"
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getHubContext());

        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        HobsonPlugin plugin = pluginManager.getLocalPlugin(pctx);
        if (plugin != null) {
            PropertyContainerClass pcc = plugin.getConfigurationClass();
            if (pcc != null) {
                return new JsonRepresentation(DTOMapper.mapPropertyContainerClass(pcc, true, linkProvider).toJSON());
            } else {
                throw new HobsonNotFoundException("Plugin configuration class not found");
            }
        } else {
            throw new HobsonNotFoundException("Plugin not found");
        }
    }
}

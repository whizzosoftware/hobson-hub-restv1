/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.config.Configuration;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
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
public class PluginConfigurationResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/configuration";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/configuration Get plugin configuration
     * @apiVersion 0.1.6
     * @apiName GetPluginConfiguration
     * @apiDescription Retrieves a plugin's configuration.
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response:
     * {
     *   "properties": {
     *     "serial.port": {
     *       "name": "Serial Port",
     *       "description": "The serial port the RA-RS232 controller is connected to (should not be used with Serial Hostname)",
     *       "type": "STRING",
     *     },
     *     "serial.hostname": {
     *       "name": "Serial Hostname",
     *       "description": "The hostname of the GlobalCache device that RA-RS232 controller is connected to (should not be used with Serial Port)",
     *       "value": "192.168.0.200",
     *       "type": "STRING",
     *     }
     *   },
     *   "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/configuration"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        String pluginId = getAttribute("pluginId");
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        Configuration config = pluginManager.getPluginConfiguration(ctx.getUserId(), ctx.getHubId(), pluginId);
        return new JsonRepresentation(JSONSerializationHelper.createPluginConfigPropertiesJSON(pluginId, config));
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/configuration Set plugin configuration
     * @apiVersion 0.1.6
     * @apiName SetPluginConfiguration
     * @apiDescription Sets a plugin's configuration.
     * @apiGroup Plugin
     * @apiExample {json} Example Request:
     * {
     *   "properties": {
     *     "serial.hostname": {
     *       "value": "192.168.0.200"
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
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        String pluginId = getAttribute("pluginId");
        pluginManager.setPluginConfiguration(ctx.getUserId(), ctx.getHubId(), pluginId, JSONSerializationHelper.createConfiguration(JSONHelper.createJSONFromRepresentation(entity)));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

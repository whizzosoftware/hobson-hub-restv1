/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginList;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import org.json.JSONArray;
import org.restlet.data.Form;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource for obtaining a list of plugins.
 *
 * @author Dan Noguerol
 */
public class PluginsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins";
    public static final String REL = "plugins";

    @Inject
    Authorizer authorizer;
    @Inject
    PluginManager pluginManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins Get plugins
     * @apiParam {Boolean} remote If true, then both local and remote plugin information is returned (defaults to false)
     * @apiParam {Boolean} details If true, then detailed plugin information is included in the response (defaults to false)
     * @apiVersion 0.1.6
     * @apiName GetPlugins
     * @apiDescription Retrieves a list of plugins.
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response (details == false):
     * [
     *   {
     *     "id": "com.whizzosoftware.hobson.server-radiora",
     *     "name": "Hobson RadioRa Plugin",
     *     "type": "PLUGIN",
     *     "status": {
     *       "status": "RUNNING"
     *     },
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora",
     *       "devices": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices"
     *     }
     *   },
     *   {
     *     "id": "com.whizzosoftware.hobson.server-openweathermap",
     *     "name": "Hobson OpenWeatherMap Plugin",
     *     "type": "PLUGIN",
     *     "status": {
     *       "status": "FAILED"
     *     },
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-openweathermap"
     *     }
     *   }
     * ]
     * @apiSuccessExample {json} Success Response (details == true):
     * [
     *   {
     *     "id": "com.whizzosoftware.hobson.server-radiora",
     *     "name": "Hobson RadioRa Plugin",
     *     "type": "PLUGIN",
     *     "status": {
     *       "status": "RUNNING"
     *     },
     *     "currentVersion": "0.0.2.SNAPSHOT",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora",
     *       "configuration": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/configuration",
     *       "reload": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/reload"
     *       "devices": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices"
     *     }
     *   },
     *   {
     *     "id": "com.whizzosoftware.hobson.server-openweathermap",
     *     "name": "Hobson OpenWeatherMap Plugin",
     *     "type": "PLUGIN",
     *     "status": {
     *       "message": "Zip code is not set in plugin configuration",
     *       "status": "FAILED"
     *     },
     *     "currentVersion": "0.0.1.SNAPSHOT",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-openweathermap",
     *       "configuration": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-openweathermap/configuration",
     *       "reload": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-openweathermap/reload",
     *       "update": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-nest/0.0.4/install",
     *       "devices": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-nest/devices"
     *     }
     *   }
     * ]
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        Form queryParams = getQuery();
        Boolean remote = getBooleanParam(queryParams.getFirstValue("remote"));
        Boolean details = getBooleanParam(queryParams.getFirstValue("details"));

        // generate a plugin list
        PluginList bl = pluginManager.getPluginDescriptors(ctx.getHubContext(), remote);

        // return a 200 with the JSON
        JSONArray results = new JSONArray();
        for (PluginDescriptor pd : bl.getPlugins()) {
            results.put(linkHelper.addPluginDescriptorLinks(
                ctx,
                JSONSerializationHelper.createPluginDescriptorJSON(
                    pd,
                    details
                ),
                pd,
                details
            ));
        }
        return new JsonRepresentation(results);
    }

    private Boolean getBooleanParam(String param) {
        Boolean b = Boolean.FALSE;
        if (param != null) {
            b = Boolean.parseBoolean(param);
        }
        return b;
    }
}

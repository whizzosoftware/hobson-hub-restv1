/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.api.hub.HobsonHub;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.DTOBuildContext;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.IdProvider;
import com.whizzosoftware.hobson.dto.hub.HobsonHubDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaVariableProxyProvider;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource for retrieving hub information.
 *
 * @author Dan Noguerol
 */
public class HubResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    TaskManager taskManager;
    @Inject
    PresenceManager presenceManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId Get Hub details
     * @apiVersion 0.1.6
     * @apiName GetHubDetails
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "actionClasses", "conditionClasses", "configuration", "configurationClass", "devices", "log", "localPlugins", "remotePlugins", "tasks").
     * @apiDescription Retrieves details about a Hub. This provides the API version number as well as links to other relevant resources.
     * @apiGroup Hub
     * @apiSuccess {Object} actionClasses The action classes published to the Hub.
     * @apiSuccess {Object} conditionClasses The condition classes published to the Hub.
     * @apiSuccess {Object} configuration The current Hub configuration values.
     * @apiSuccess {Object} configurationClass The Hub configuration class.
     * @apiSuccess {Object} devices The devices published to the Hub.
     * @apiSuccess {Object} log The Hub log.
     * @apiSuccess {String} name The name of the Hub.
     * @apiSuccess {Object} localPlugins The plugins locally installed on the Hub.
     * @apiSuccess {Object} remotePlugins The plugins remotely available to install on the Hub.
     * @apiSuccess {Object} tasks The tasks that have been created on the Hub.
     * @apiSuccess {String} version The current Hub version.
     * @apiSuccessExample Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local",
     *   "actionClasses": {"@id": "/api/v1/users/local/hubs/local/tasks/actionClasses"},
     *   "conditionClasses": {"@id": "/api/v1/users/local/hubs/local/tasks/conditionClasses"},
     *   "configuration": {"@id": "/api/v1/users/local/hubs/local/configuration"},
     *   "configurationClass": {"@id": "/api/v1/users/local/hubs/local/configurationClass"},
     *   "devices": {"@id": "/api/v1/users/local/hubs/local/devices"},
     *   "log": {"@id": "/api/v1/users/local/hubs/local/log"},
     *   "name": "Unnamed",
     *   "localPlugins": {"@id": "/api/v1/users/local/hubs/local/plugins/local"},
     *   "remotePlugins": {"@id": "/api/v1/users/local/hubs/local/plugins/remote"},
     *   "tasks": {"@id": "/api/v1/users/local/hubs/local/tasks"},
     *   "version": "0.5.0.SNAPSHOT"
     * }
     *
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        expansions.add(JSONAttributes.ITEM);

        authorizer.authorizeHub(ctx.getHubContext());

        HobsonHub hub = hubManager.getHub(ctx.getHubContext());
        HobsonHubDTO dto = new HobsonHubDTO.Builder(
            new DTOBuildContext.Builder().
                hubManager(hubManager).
                pluginManager(pluginManager).
                deviceManager(deviceManager).
                variableManager(variableManager).
                taskManager(taskManager).
                presenceManager(presenceManager).
                expansionFields(expansions).
                idProvider(idProvider).
                addProxyValueProvider(new MediaVariableProxyProvider(ctx)).
                build(),
            hub,
            true
        ).build();

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getMediaType() + "+json"));
        return jr;
    }
}

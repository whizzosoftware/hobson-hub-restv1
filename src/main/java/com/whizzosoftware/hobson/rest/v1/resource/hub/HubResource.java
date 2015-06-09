/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.ExpansionFields;
import com.whizzosoftware.hobson.api.hub.HobsonHub;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOHelper;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
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
    TaskManager taskManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId Get Hub details
     * @apiVersion 0.1.6
     * @apiName GetHubDetails
     * @apiParam (Query Parameters) {String} expand A comma-separated list of fields to expand in the response. Valid field values are "configurationClass, configuration, actionClasses, conditionClasses".
     * @apiDescription Retrieves details about a Hub. This provides the API version number as well as links to other relevant resources.
     * @apiGroup Hub
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

        authorizer.authorizeHub(ctx.getHubContext());

        HobsonHub hub = hubManager.getHub(ctx.getHubContext());

        // create the response DTO
        HobsonHubDTO dto = new HobsonHubDTO(linkHelper.createHubLink(hub.getContext()), hub.getName(), hub.getVersion());

        // add action classes
        ItemListDTO ildto = new ItemListDTO(linkHelper.createTaskActionClassesLink(ctx.getHubContext()));
        if (expansions.has("actionClasses")) {
            for (PropertyContainerClass tac : taskManager.getAllActionClasses(hub.getContext())) {
                ildto.add(new PropertyContainerClassDTO(linkHelper.createTaskActionClassLink(tac.getContext())));
            }
        }
        dto.setActionClasses(ildto);

        // add configuration class attribute
        if (expansions.has("configurationClass")) {
            dto.setConfigurationClass(
                new PropertyContainerClassDTO(
                    linkHelper.createHubConfigurationClassLink(hub.getContext()),
                    hub.getConfigurationClass().getName(),
                    DTOHelper.mapTypedPropertyList(hub.getConfigurationClass().getSupportedProperties())
                )
            );
        } else {
            dto.setConfigurationClass(new PropertyContainerClassDTO(linkHelper.createHubConfigurationClassLink(hub.getContext())));
        }

        // add configuration attribute
        if (expansions.has("configuration")) {
            PropertyContainer hubConfig = hubManager.getConfiguration(hub.getContext());
            dto.setConfiguration(
                new PropertyContainerDTO(
                    linkHelper.createHubConfigurationLink(hub.getContext()),
                    hubConfig.getName(),
                    new PropertyContainerClassDTO(
                        linkHelper.createHubConfigurationClassLink(ctx.getHubContext())
                    ),
                    hubConfig.getPropertyValues()
                )
            );
        } else {
            dto.setConfiguration(new PropertyContainerDTO(linkHelper.createHubConfigurationLink(hub.getContext())));
        }

        // add condition classes
        ildto = new ItemListDTO(linkHelper.createTaskConditionClassesLink(ctx.getHubContext()));
        if (expansions.has("conditionClasses")) {
            for (PropertyContainerClass tcc : taskManager.getAllConditionClasses(hub.getContext())) {
                ildto.add(new PropertyContainerClassDTO(linkHelper.createTaskConditionClassLink(tcc.getContext())));
            }
        }
        dto.setConditionClasses(ildto);

        // add devices attribute
        ildto = new ItemListDTO(linkHelper.createDevicesLink(ctx.getHubContext()));
        dto.setDevices(ildto);

        // add log attribute
        dto.setLog(new HubLogDTO(linkHelper.createHubLogLink(ctx.getHubContext())));

        // add local plugins attribute
        ildto = new ItemListDTO(linkHelper.createLocalPluginsLink(ctx.getHubContext()));
        dto.setLocalPlugins(ildto);
        if (expansions.has("localPlugins")) {
            for (PluginDescriptor pd : pluginManager.getLocalPluginDescriptors(ctx.getHubContext())) {
                PluginContext pctx = PluginContext.create(ctx.getHubContext(), pd.getId());
                HobsonPluginDTO.Builder builder = new HobsonPluginDTO.Builder(linkHelper.createLocalPluginLink(pctx));
                DTOHelper.populatePluginDTO(
                    pd,
                    pd.isConfigurable() ? linkHelper.createLocalPluginConfigurationClassLink(pctx) : null,
                    pd.isConfigurable() ? pluginManager.getPlugin(pctx).getConfigurationClass() : null,
                    pd.isConfigurable() ? linkHelper.createLocalPluginConfigurationLink(pctx) : null,
                    pd.isConfigurable() ? pluginManager.getPluginConfiguration(pctx) : null,
                    linkHelper.createLocalPluginIconLink(pctx),
                    builder
                );
            }
        }

        // add remote plugins attribute
        ildto = new ItemListDTO(linkHelper.createRemotePluginsLink(ctx.getHubContext()));
        dto.setRemotePlugins(ildto);
        if (expansions.has("remotePlugins")) {
            for (PluginDescriptor pd : pluginManager.getRemotePluginDescriptors(ctx.getHubContext())) {
                PluginContext pctx = PluginContext.create(ctx.getHubContext(), pd.getId());
                HobsonPluginDTO.Builder builder = new HobsonPluginDTO.Builder(linkHelper.createLocalPluginLink(pctx));
                DTOHelper.populatePluginDTO(
                    pd,
                    null,
                    null,
                    null,
                    null,
                    null,
                    builder
                );
                builder.addLink("install", linkHelper.createRemotePluginInstallLink(pctx, pd.getVersionString()));
                ildto.add(builder.build());
            }
        }

        // add tasks
        ildto = new ItemListDTO(linkHelper.createTasksLink(ctx.getHubContext()));
        if (expansions.has("tasks")) {
            ildto.updateNumberOfItems();
            for (HobsonTask task : taskManager.getAllTasks(hub.getContext())) {
                HobsonTaskDTO.Builder builder = new HobsonTaskDTO.Builder(linkHelper.createTaskLink(task.getContext()));
                builder.name(task.getName())
                    .conditionSet(new PropertyContainerSetDTO("", null, null))
                    .actionSet(new PropertyContainerSetDTO("", null, null))
                    .properties(task.getProperties());
                ildto.add(builder.build());
            }
        }
        dto.setTasks(ildto);

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON(linkHelper));
        jr.setMediaType(new MediaType(dto.getMediaType() + "+json"));
        return jr;
    }
}

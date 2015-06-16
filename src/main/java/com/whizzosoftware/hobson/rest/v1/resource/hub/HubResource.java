/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.rest.ExpansionFields;
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
import com.whizzosoftware.hobson.dto.hub.HobsonHubDTO;
import com.whizzosoftware.hobson.dto.hub.HubLogDTO;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerSetDTO;
import com.whizzosoftware.hobson.dto.task.HobsonTaskDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOHelper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
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
    LinkProvider linkProvider;

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
        HobsonHubDTO.Builder builder = new HobsonHubDTO.Builder(linkProvider.createHubLink(hub.getContext()))
            .name(hub.getName())
            .version(hub.getVersion());

        // add action classes
        ItemListDTO ildto = new ItemListDTO(linkProvider.createTaskActionClassesLink(ctx.getHubContext()));
        if (expansions.has("actionClasses")) {
            for (PropertyContainerClass tac : taskManager.getAllActionClasses(hub.getContext())) {
                ildto.add(new PropertyContainerClassDTO.Builder(linkProvider.createTaskActionClassLink(tac.getContext())).build());
            }
        }
        builder.actionClasses(ildto);

        // add configuration class attribute
        if (expansions.has("configurationClass")) {
            builder.configurationClass(
                new PropertyContainerClassDTO.Builder(linkProvider.createHubConfigurationClassLink(hub.getContext()))
                    .name(hub.getConfigurationClass().getName())
                    .supportedProperties(DTOHelper.mapTypedPropertyList(hub.getConfigurationClass().getSupportedProperties()))
                    .build()
            );
        } else {
            builder.configurationClass(new PropertyContainerClassDTO.Builder(linkProvider.createHubConfigurationClassLink(hub.getContext())).build());
        }

        // add configuration attribute
        if (expansions.has("configuration")) {
            PropertyContainer hubConfig = hubManager.getConfiguration(hub.getContext());
            builder.configuration(
                new PropertyContainerDTO.Builder(linkProvider.createHubConfigurationLink(hub.getContext()))
                    .name(hubConfig.getName())
                    .containerClass(
                        new PropertyContainerClassDTO.Builder(linkProvider.createHubConfigurationClassLink(ctx.getHubContext())).build()
                    )
                    .values(hubConfig.getPropertyValues())
                    .build()
                );
        } else {
            builder.configuration(new PropertyContainerDTO.Builder(linkProvider.createHubConfigurationLink(hub.getContext())).build());
        }

        // add condition classes
        ildto = new ItemListDTO(linkProvider.createTaskConditionClassesLink(ctx.getHubContext()));
        if (expansions.has("conditionClasses")) {
            for (PropertyContainerClass tcc : taskManager.getAllConditionClasses(hub.getContext())) {
                ildto.add(new PropertyContainerClassDTO.Builder(linkProvider.createTaskConditionClassLink(tcc.getContext())).build());
            }
        }
        builder.conditionClasses(ildto);

        // add devices attribute
        ildto = new ItemListDTO(linkProvider.createDevicesLink(ctx.getHubContext()));
        builder.devices(ildto);

        // add log attribute
        builder.log(new HubLogDTO(linkProvider.createHubLogLink(ctx.getHubContext())));

        // add local plugins attribute
        ildto = new ItemListDTO(linkProvider.createLocalPluginsLink(ctx.getHubContext()));
        builder.localPlugins(ildto);
        if (expansions.has("localPlugins")) {
            for (PluginDescriptor pd : pluginManager.getLocalPluginDescriptors(ctx.getHubContext())) {
                PluginContext pctx = PluginContext.create(ctx.getHubContext(), pd.getId());
                HobsonPluginDTO.Builder builder2 = new HobsonPluginDTO.Builder(linkProvider.createLocalPluginLink(pctx));
                DTOHelper.populatePluginDTO(
                    pd,
                    pd.isConfigurable() ? linkProvider.createLocalPluginConfigurationClassLink(pctx) : null,
                    pd.isConfigurable() ? pluginManager.getPlugin(pctx).getConfigurationClass() : null,
                    pd.isConfigurable() ? linkProvider.createLocalPluginConfigurationLink(pctx) : null,
                    pd.isConfigurable() ? pluginManager.getPluginConfiguration(pctx) : null,
                    linkProvider.createLocalPluginIconLink(pctx),
                    builder2
                );
            }
        }

        // add remote plugins attribute
        ildto = new ItemListDTO(linkProvider.createRemotePluginsLink(ctx.getHubContext()));
        builder.remotePlugins(ildto);
        if (expansions.has("remotePlugins")) {
            for (PluginDescriptor pd : pluginManager.getRemotePluginDescriptors(ctx.getHubContext())) {
                PluginContext pctx = PluginContext.create(ctx.getHubContext(), pd.getId());
                HobsonPluginDTO.Builder builder2 = new HobsonPluginDTO.Builder(linkProvider.createLocalPluginLink(pctx));
                DTOHelper.populatePluginDTO(
                    pd,
                    null,
                    null,
                    null,
                    null,
                    null,
                    builder2
                );
                builder2.addLink("install", linkProvider.createRemotePluginInstallLink(pctx, pd.getVersionString()));
                ildto.add(builder.build());
            }
        }

        // add tasks
        ildto = new ItemListDTO(linkProvider.createTasksLink(ctx.getHubContext()));
        if (expansions.has("tasks")) {
            for (HobsonTask task : taskManager.getAllTasks(hub.getContext())) {
                HobsonTaskDTO.Builder builder2 = new HobsonTaskDTO.Builder(linkProvider.createTaskLink(task.getContext()));
                builder2.name(task.getName())
                    .conditionSet(new PropertyContainerSetDTO.Builder("").build()) // TODO
                    .actionSet(new PropertyContainerSetDTO.Builder("").build()) // TODO
                    .properties(task.getProperties());
                ildto.add(builder.build());
            }
        }
        builder.tasks(ildto);

        HobsonHubDTO dto = builder.build();
        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getMediaType() + "+json"));
        return jr;
    }
}

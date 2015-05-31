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
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
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
     *   "name": "Unnamed",
     *   "version": "0.5.0.SNAPSHOT"
     * }
     *
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        // return the JSON response
        HobsonHub hub = hubManager.getHub(ctx.getHubContext());
        HobsonHubDTO dto = new HobsonHubDTO(hub, linkHelper);

        // add configuration class attribute
        if (expansions.has("configurationClass")) {
            dto.setConfigurationClass(
                new PropertyContainerClassDTO(linkHelper.createHubConfigurationClassLink(hub.getContext()), hub.getConfigurationClass())
            );
        } else {
            dto.setConfigurationClass(new PropertyContainerClassDTO(linkHelper.createHubConfigurationClassLink(hub.getContext())));
        }

        // add configuration attribute
        if (expansions.has("configuration")) {
            dto.setConfiguration(
                new PropertyContainerDTO(linkHelper.createHubConfigurationLink(hub.getContext()), hub.getConfiguration())
            );
        } else {
            dto.setConfiguration(new PropertyContainerDTO(linkHelper.createHubConfigurationLink(hub.getContext())));
        }

        // add plugins attribute
        ItemListDTO ildto = new ItemListDTO(linkHelper.createPluginsLink(ctx.getHubContext()));
        dto.setPlugins(ildto);

        // add devices attribute
        ildto = new ItemListDTO(linkHelper.createDevicesLink(ctx.getHubContext()));
        dto.setDevices(ildto);

        // add condition classes
        ildto = new ItemListDTO(linkHelper.createTaskConditionClassesLink(ctx.getHubContext()));
        if (expansions.has("conditionClasses")) {
            for (PropertyContainerClass tcc : taskManager.getAllConditionClasses(hub.getContext())) {
                ildto.add(new PropertyContainerClassDTO(linkHelper.createTaskConditionClassLink(tcc.getContext()), tcc));
            }
        }
        dto.setConditionClasses(ildto);

        // add action classes
        ildto = new ItemListDTO(linkHelper.createTaskActionClassesLink(ctx.getHubContext()));
        if (expansions.has("actionClasses")) {
            for (PropertyContainerClass tac : taskManager.getAllActionClasses(hub.getContext())) {
                ildto.add(new PropertyContainerClassDTO(linkHelper.createTaskActionClassLink(tac.getContext()), tac));
            }
        }
        dto.setActionClasses(ildto);

        // add tasks
        ildto = new ItemListDTO(linkHelper.createTasksLink(ctx.getHubContext()));
        if (expansions.has("tasks")) {
            ildto.updateNumberOfItems();
            for (HobsonTask task : taskManager.getAllTasks(hub.getContext())) {
                ildto.add(new HobsonTaskDTO(linkHelper.createTaskLink(task.getContext()), task, linkHelper));
            }
        }
        dto.setTasks(ildto);

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON(linkHelper));
        jr.setMediaType(new MediaType(dto.getMediaType() + "+json"));
        return jr;
    }
}

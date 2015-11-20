/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class TaskActionClassesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks/actionClasses";

    @Inject
    TaskManager taskManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/actionClasses Get all action classes
     * @apiVersion 0.5.0
     * @apiName GetAllActionClasses
     * @apiDescription Retrieves a list of all available action classes (regardless of plugin).
     * @apiGroup Tasks
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (the only supported value is "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/tasks/actionClasses",
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-actions/actionClasses/turnOn"
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-actions/actionClasses/log"
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        boolean expandItems = expansions.has("item");
        boolean applyConstraints = Boolean.parseBoolean(getQueryValue("constraints"));

        ItemListDTO results = new ItemListDTO(idProvider.createTaskActionClassesId(ctx.getHubContext()));
        for (PropertyContainerClass actionClass : taskManager.getAllActionClasses(ctx.getHubContext(), applyConstraints)) {
            PropertyContainerClassDTO.Builder builder = new PropertyContainerClassDTO.Builder(
                idProvider.createTaskActionClassId(actionClass.getContext())
            );
            if (expandItems) {
                builder.name(actionClass.getName())
                    .descriptionTemplate(actionClass.getDescriptionTemplate())
                    .supportedProperties(DTOMapper.mapTypedPropertyList(actionClass.getSupportedProperties()));
            }
            results.add(builder.build());
        }
        return new JsonRepresentation(results.toJSON());
    }
}

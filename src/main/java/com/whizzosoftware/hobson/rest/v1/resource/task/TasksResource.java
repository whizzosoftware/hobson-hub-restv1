/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.task.HobsonTaskDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
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

import javax.inject.Inject;
import java.util.Collection;

/**
 * A REST resource for retrieving a list of all tasks.
 *
 * @author Dan Noguerol
 */
public class TasksResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;
    @Inject
    TaskManager taskManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/tasks Get all tasks
     * @apiVersion 0.1.3
     * @apiName GetAllTasks
     * @apiDescription Retrieves a list of all tasks (regardless of provider).
     * @apiGroup Tasks
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 1,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/tasks/112c8933-f487-4eb5-ba44-1ea8d4691fd9",
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        ItemListDTO results = new ItemListDTO(linkProvider.createTasksLink(ctx.getHubContext()));
        boolean expandItems = expansions.has("item");

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return hubManager.getContainerClass(ctx);
            }
        };

        Collection<HobsonTask> tasks = taskManager.getAllTasks(ctx.getHubContext());
        for (HobsonTask task : tasks) {
            HobsonTaskDTO.Builder builder = new HobsonTaskDTO.Builder(linkProvider.createTaskLink(task.getContext()));
            if (expandItems) {
                builder.name(task.getName());
                builder.description(task.getDescription());
                builder.conditions(DTOMapper.mapPropertyContainerList(task.getConditions(), false, pccp, linkProvider));
                builder.actionSet(DTOMapper.mapPropertyContainerSet(task.getActionSet(), false, pccp, linkProvider));
                builder.properties(task.getProperties());
            }
            results.add(builder.build());
        }

        return new JsonRepresentation(results.toJSON());
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/tasks Create task
     * @apiVersion 0.1.3
     * @apiName AddTask
     * @apiDescription Creates a new task.
     * @apiGroup Tasks
     * @apiExample Example Request (scheduled task):
     * {
     *   "name": "My Task",
     *   "conditions": [
     *     {
     *       "cclass": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule"
     *       },
     *       "values": {
     *         "date": "20140701",
     *         "time": "100000Z",
     *         "recurrence": "FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13"
     *       }
     *     }
     *   ],
     *   "actionSet": {
     *     "actions": [
     *       {
     *         "cclass": {
     *           "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-actions/actionClasses/log"
     *         },
     *         "values": {
     *           "message": "Foo"
     *         }
     *       }
     *     ]
     *   }
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());

        HobsonTaskDTO dto = new HobsonTaskDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        dto.validate();

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return hubManager.getContainerClass(ctx);
            }
        };

        taskManager.createTask(
            ctx.getHubContext(),
            dto.getName(),
            dto.getDescription(),
            DTOMapper.mapPropertyContainerDTOList(dto.getConditions(), pccp, linkProvider),
            DTOMapper.mapPropertyContainerSetDTO(dto.getActionSet(), pccp, linkProvider)
        );

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

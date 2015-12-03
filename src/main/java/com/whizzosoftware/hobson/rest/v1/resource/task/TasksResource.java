/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.task.HobsonTaskDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.MediaType;
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
    HubManager hubManager;
    @Inject
    TaskManager taskManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

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
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        ItemListDTO results = new ItemListDTO(idProvider.createTasksId(ctx.getHubContext()));
        boolean showDetails = expansions.has("item");

        Collection<HobsonTask> tasks = taskManager.getAllTasks(ctx.getHubContext());

        if (tasks != null) {
            expansions.pushContext(JSONAttributes.ITEM);
            for (HobsonTask task : tasks) {
                HobsonTaskDTO dto = new HobsonTaskDTO.Builder(
                    dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                    task,
                    showDetails
                ).build();
                results.add(dto);
            }
            expansions.popContext();
        }

        JsonRepresentation jr = new JsonRepresentation(results.toJSON());
        jr.setMediaType(new MediaType(results.getJSONMediaType()));
        return jr;
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
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

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
            DTOMapper.mapPropertyContainerDTOList(dto.getConditions(), pccp, idProvider),
            DTOMapper.mapPropertyContainerSetDTO(dto.getActionSet(), pccp, idProvider)
        );

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/tasks Delete all tasks
     * @apiVersion 0.7.0
     * @apiName DeleteTasks
     * @apiDescription Deletes all tasks that have been created.
     * @apiGroup Tasks
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        for (HobsonTask task : taskManager.getAllTasks(ctx.getHubContext())) {
            taskManager.deleteTask(task.getContext());
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskContext;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.task.HobsonTaskDTO;
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

/**
 * A REST resource for managing a particular task.
 *
 * @author Dan Noguerol
 */
public class TaskResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks/{taskId}";

    @Inject
    HubManager hubManager;
    @Inject
    TaskManager taskManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/tasks/:taskId Get task details
     * @apiVersion 0.5.0
     * @apiName GetTask
     * @apiDescription Retrieves details about a specific task.
     * @apiGroup Tasks
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (the only supported value is "actionSet").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/tasks/112c8933-f487-4eb5-ba44-1ea8d4691fd9",
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
     *     "@id": "/api/v1/users/local/hubs/local/tasks/actionSets/dc419994-987f-4bff-81f9-e5bce53733f3"
     *   },
     *   "properties": {
     *     "scheduled": false,
     *     "nextRunTime": 1447408800000
     *   }
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        HobsonTask task = taskManager.getTask(TaskContext.create(ctx.getHubContext(), getAttribute("taskId")));

        HobsonTaskDTO dto = new HobsonTaskDTO.Builder(
            dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
            task,
            true
        ).build();

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(new MediaType(dto.getJSONMediaType()));
        return jr;
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/tasks/:taskId Update task
     * @apiVersion 0.5.0
     * @apiName UpdateTask
     * @apiDescription Updated an existing task.
     * @apiGroup Tasks
     * @apiExample Example Request (simple event task):
     * {
     *   "name": "My Task",
     *   "conditionSet": {
     *     "trigger": {
     *       "cclass": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule"
     *       },
     *       "values": {
     *         "date": "20140701",
     *         "time": "100000Z",
     *         "recurrence": "FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13"
     *       }
     *     }
     *   },
     *   "actionSet": {
     *     "@id": "/api/v1/users/local/hubs/local/tasks/actionSets/dc419994-987f-4bff-81f9-e5bce53733f3"
     *   }
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        DTOMapper mapper = new DTOMapper(); // TODO: inject

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return hubManager.getContainerClass(ctx);
            }
        };

        HobsonTaskDTO dto = new HobsonTaskDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        taskManager.updateTask(
            TaskContext.create(ctx.getHubContext(), getAttribute("taskId")),
            dto.getName(),
            dto.getDescription(),
            mapper.mapPropertyContainerDTOList(dto.getConditions(), pccp, idProvider),
            mapper.mapPropertyContainerSetDTO(dto.getActionSet(), pccp, idProvider));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/tasks/:taskId Delete task
     * @apiVersion 0.1.3
     * @apiName DeleteTask
     * @apiDescription Deletes a specific task.
     * @apiGroup Tasks
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        taskManager.deleteTask(TaskContext.create(ctx.getHubContext(), getAttribute("taskId")));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

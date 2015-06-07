/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.ExpansionFields;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.HobsonTaskDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOHelper;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
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
    public static final String REL = "tasks";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;
    @Inject
    TaskManager taskManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/tasks Get all tasks
     * @apiVersion 0.1.3
     * @apiName GetAllTasks
     * @apiDescription Retrieves a list of all tasks (regardless of provider).
     * @apiGroup Tasks
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "My Task",
     *     "type": "EVENT",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-rules/tasks/efc02d7a-d0e0-46fb-9cc3-2ca70a66dc05"
     *     },
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        ItemListDTO results = new ItemListDTO(linkHelper.createTasksLink(ctx.getHubContext()));

        Collection<HobsonTask> tasks = taskManager.getAllTasks(ctx.getHubContext());
        for (HobsonTask task : tasks) {
            HobsonTaskDTO.Builder builder = new HobsonTaskDTO.Builder(linkHelper.createTaskLink(task.getContext()));
            if (expansions.has("item")) {
                builder.name(task.getName());
                builder.description(task.getDescription());
                builder.conditionSet(DTOHelper.mapPropertyContainerSet(task.getConditionSet()));
                builder.actionSet(DTOHelper.mapPropertyContainerSet(task.getActionSet()));
                builder.properties(task.getProperties());
            }
            results.add(builder.build());
        }

        return new JsonRepresentation(results.toJSON(linkHelper));
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/tasks Create task
     * @apiVersion 0.1.3
     * @apiName AddTask
     * @apiDescription Creates a new task.
     * @apiGroup Tasks
     * @apiExample Example Request (scheduled task):
     * {
     *   "name": "My Scheduled Task",
     *   "triggerCondition": {
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub.scheduler",
     *     "date": "20140701",
     *     "time": "100000",
     *     "recurrence": "FREQ=MINUTELY;INTERVAL=1"
     *   },
     *   "actions": [{
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-actions",
     *     "actionId": "log",
     *     "name": "My Action 2",
     *   }],
     *   "properties": {
     *     "nextRunTime": 1234567890
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

        taskManager.createTask(
            ctx.getHubContext(),
            dto.getName(),
            dto.getDescription(),
            DTOHelper.mapPropertyContainerSetDTO(dto.getConditionSet(), hubManager, linkHelper),
            DTOHelper.mapPropertyContainerSetDTO(dto.getActionSet(), hubManager, linkHelper)
        );

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

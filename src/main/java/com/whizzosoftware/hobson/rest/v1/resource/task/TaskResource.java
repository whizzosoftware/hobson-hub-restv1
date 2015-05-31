/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskContext;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.HobsonTaskDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
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
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/tasks/{taskId}";

    @Inject
    Authorizer authorizer;
    @Inject
    TaskManager taskManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/tasks/:taskId Get task details
     * @apiVersion 0.1.3
     * @apiName GetTask
     * @apiDescription Retrieves details about a specific task.
     * @apiGroup Tasks
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "My Task",
     *     "type": "EVENT",
     *     "provider": "com.whizzosoftware.hobson.hub-rules",
     *     "conditions": [{
     *       "event": "variableUpdate",
     *       "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-sample",
     *       "deviceId": "switch",
     *       "name": "on",
     *       "comparator": "=",
     *       "value": true,
     *       "changeId": "turnOn"
     *     }],
     *     "actions": [{
     *       "pluginId": "com.whizzosoftware.hobson.hub-actions",
     *       "actionId": "log",
     *       "name": "My Action 1",
     *       "properties": {
     *         "message": "Sample log entry"
     *       }
     *     }],
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-rules/tasks/efc02d7a-d0e0-46fb-9cc3-2ca70a66dc05"
     *     },
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        HobsonTask task = taskManager.getTask(TaskContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("taskId")));
        return new JsonRepresentation(new HobsonTaskDTO(linkHelper.createTaskLink(task.getContext()), task, linkHelper));
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/tasks/:taskId Update task
     * @apiVersion 0.1.3
     * @apiName UpdateTask
     * @apiDescription Updated an existing task.
     * @apiGroup Tasks
     * @apiExample Example Request (simple event task):
     * {
     *   "name": "My Event Task",
     *   "provider": "com.whizzosoftware.hobson.hub.hobson-hub-rules",
     *   "conditions": [{
     *     "event": "variableUpdate",
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-zwave",
     *     "deviceId": "zwave-32",
     *     "changeId": "turnOff"
     *   }],
     *   "actions": [{
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-actions",
     *     "actionId": "log",
     *     "name": "My Action 1",
     *     "properties": {
     *       "message": "Event task fired"
     *     }
     *   }]
     * }
     * @apiExample Example Request (advanced event task):
     * {
     *   "name": "My Event Task",
     *   "provider": "com.whizzosoftware.hobson.hub.hobson-hub-rules",
     *   "conditions": [{
     *     "event": "variableUpdate",
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-zwave",
     *     "deviceId": "zwave-32",
     *     "variable": {
     *       "name": "on",
     *       "comparator": "eq",
     *       "value": true
     *     }
     *   }],
     *   "actions": [{
     *     "pluginId": "com.whizzosoftware.hobson.hub.hobson-hub-actions",
     *     "actionId": "log",
     *     "name": "My Action 1",
     *     "properties": {
     *       "message": "Event task fired"
     *     }
     *   }]
     * }
     * @apiExample Example Request (scheduled task):
     * {
     *   "name": "My Scheduled Task",
     *   "provider": "com.whizzosoftware.hobson.hub.hobson-hub-scheduler",
     *   "conditions": [{
     *     "start": "20140701T100000",
     *     "recurrence": "FREQ=MINUTELY;INTERVAL=1"
     *   }],
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
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());

        DTOMapper mapper = new DTOMapper(); // TODO: inject

        HobsonTaskDTO dto = new HobsonTaskDTO(JSONHelper.createJSONFromRepresentation(entity));
        taskManager.updateTask(
            TaskContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("taskId")),
            dto.getName(),
            mapper.mapPropertyContainerSetDTO(dto.getConditionSet(), linkHelper),
            mapper.mapPropertyContainerSetDTO(dto.getActionSet(), linkHelper)
        );

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/tasks/:taskId Delete task
     * @apiVersion 0.1.3
     * @apiName DeleteTask
     * @apiDescription Deletes a specific task.
     * @apiGroup Tasks
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        taskManager.deleteTask(TaskContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("taskId")));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

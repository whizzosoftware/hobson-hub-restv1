/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.json.JSONObject;
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
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks/{providerId}/{taskId}";

    @Inject
    TaskManager taskManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/tasks/:providerId/:taskId Get task details
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
     *       "leftTerm": "foo",
     *       "op": "eq",
     *       "rightTerm": "bar"
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
     *       "self": "/api/tasks/com.whizzosoftware.hobson.server-rules/efc02d7a-d0e0-46fb-9cc3-2ca70a66dc05"
     *     },
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(JSONMarshaller.createTaskJSON(ctx, taskManager.getTask(ctx.getUserId(), ctx.getHubId(), getAttribute("providerId"), getAttribute("taskId")), true, true));
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/tasks/:providerId/:taskId Update task
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
        JSONObject json = JSONMarshaller.createJSONFromRepresentation(entity);
        taskManager.updateTask(ctx.getUserId(), ctx.getHubId(), getAttribute("providerId"), getAttribute("taskId"), json);
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId/tasks/:providerId/:taskId Delete task
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
        taskManager.deleteTask(ctx.getUserId(), ctx.getHubId(), getAttribute("providerId"), getAttribute("taskId"));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

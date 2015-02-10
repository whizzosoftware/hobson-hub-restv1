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
 * A REST resource for retrieving a list of all tasks.
 *
 * @author Dan Noguerol
 */
public class TasksResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks";
    public static final String REL = "tasks";

    @Inject
    TaskManager taskManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/tasks Get all tasks
     * @apiVersion 0.1.3
     * @apiParam {Boolean} properties If true, include any properties associated with the task
     * @apiName GetAllTasks
     * @apiDescription Retrieves a list of all tasks (regardless of provider).
     * @apiGroup Tasks
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "My Task",
     *     "type": "EVENT",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/tasks/com.whizzosoftware.hobson.server-rules/efc02d7a-d0e0-46fb-9cc3-2ca70a66dc05"
     *     },
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(JSONMarshaller.createTaskListJSON(ctx, taskManager.getAllTasks(ctx.getUserId(), ctx.getHubId()), Boolean.parseBoolean(getQueryValue("properties"))));
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/tasks Create task
     * @apiVersion 0.1.3
     * @apiName AddTask
     * @apiDescription Creates a new task.
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
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        JSONObject json = JSONMarshaller.createJSONFromRepresentation(entity);
        taskManager.getPublisher().addTask(ctx.getUserId(), ctx.getHubId(), json.getString("provider"), json);
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

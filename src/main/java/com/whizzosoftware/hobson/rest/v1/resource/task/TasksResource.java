/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
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
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/tasks";
    public static final String REL = "tasks";

    @Inject
    Authorizer authorizer;
    @Inject
    TaskManager taskManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/tasks Get all tasks
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
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-rules/tasks/efc02d7a-d0e0-46fb-9cc3-2ca70a66dc05"
     *     },
     *   }
     * ]
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        boolean includeProps = Boolean.parseBoolean(getQueryValue("properties"));

        JSONArray results = new JSONArray();
        Collection<HobsonTask> tasks = taskManager.getAllTasks(ctx.getHubContext());
        for (HobsonTask task : tasks) {
            results.put(linkHelper.addTaskLinks(ctx, JSONSerializationHelper.createTaskJSON(task, false, includeProps), task.getContext().getPluginId(), task.getContext().getTaskId()));
        }
        return new JsonRepresentation(results);
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/tasks Create task
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
        authorizer.authorizeHub(ctx.getHubContext());
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
        taskManager.addTask(PluginContext.create(ctx.getHubContext(), json.getString("provider")), json);
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

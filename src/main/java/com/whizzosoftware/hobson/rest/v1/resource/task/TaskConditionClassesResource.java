package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import org.json.JSONArray;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class TaskConditionClassesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks/conditionClasses";

    @Inject
    Authorizer authorizer;
    @Inject
    TaskManager taskManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/conditionClass Get all condition classes
     * @apiVersion 0.5.0
     * @apiName GetAllConditionClasses
     * @apiDescription Retrieves a list of all available condition classes (regardless of plugin).
     * @apiGroup Tasks
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "A device or sensor turns on",
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-rules/conditionClasses/turnOn",
     *     "plugin": {
     *       "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-rules"
     *     },
     *     "propertyDescriptors": [
     *       {
     *         "description": "The device to monitor",
     *         "name": "Device",
     *         "type": "DEVICE"
     *       }
     *     ]
     *   },
     *   {
     *     "name": "A scheduled time occurs",
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule",
     *     "plugin": {
     *       "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler"
     *     },
     *     "propertyDescriptors": [
     *       {
     *         "description": "The date the task will first occur",
     *         "name": "Start date",
     *         "type": "DATE"
     *       },
     *       {
     *         "description": "The time of day the task will occur",
     *         "name": "Start time",
     *         "type": "TIME"
     *       },
     *       {
     *         "description": "How often the task should repeat",
     *         "name": "Repeat",
     *         "type": "RECURRENCE"
     *       }
     *     ]
     *   }
     * ]
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        JSONArray results = new JSONArray();
        for (PropertyContainerClass conditionClass : taskManager.getAllConditionClasses(ctx.getHubContext())) {
            results.put(new PropertyContainerClassDTO(linkHelper.createTaskConditionClassLink(conditionClass.getContext()), conditionClass).toJSON(linkHelper));
        }
        return new JsonRepresentation(results);
    }
}

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

public class TaskActionClassesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks/actionClasses";

    @Inject
    Authorizer authorizer;
    @Inject
    TaskManager taskManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/actionClasses Get all action classes
     * @apiVersion 0.5.0
     * @apiName GetAllActionClasses
     * @apiDescription Retrieves a list of all available action classes (regardless of plugin).
     * @apiGroup Tasks
     *
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "Log a message",
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-core/actionClasses/log",
     *     "plugin": {
     *       "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-core"
     *     }
     *   },
     *   {
     *     "name": "Turn on bulbs or switches",
     *     "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-core/actionClasses/turnOn",
     *     "plugin": {
     *       "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-core"
     *     },
     *     "propertyDescriptors": [
     *       {
     *         "description": "The devices to send the command to",
     *         "name": "Devices",
     *         "type": "DEVICES"
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
        for (PropertyContainerClass actionClass : taskManager.getAllActionClasses(ctx.getHubContext())) {
            results.put(new PropertyContainerClassDTO(linkHelper.createTaskActionClassLink(actionClass.getContext()), actionClass).toJSON(linkHelper));
        }
        return new JsonRepresentation(results);
    }
}

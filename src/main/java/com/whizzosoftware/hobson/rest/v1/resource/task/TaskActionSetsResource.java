package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.property.PropertyContainerSet;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.PropertyContainerSetDTO;
import com.whizzosoftware.hobson.dto.TaskActionSetDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import org.json.JSONArray;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class TaskActionSetsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks/actionSets";

    @Inject
    Authorizer authorizer;
    @Inject
    TaskManager taskManager;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/actions Get all action sets
     * @apiVersion 0.5.0
     * @apiName GetAllActionSets
     * @apiDescription Retrieves a summary list of all available action sets (regardless of plugin).
     * @apiGroup Tasks
     *
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "id": "sendEmail",
     *     "name": "Send E-mail",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-api/actions/sendEmail"
     *     }
     *   },
     *   {
     *     "id": "log",
     *     "name": "Log Message",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-api/actions/log"
     *     }
     *   }
     * ]
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        JSONArray results = new JSONArray();
        for (PropertyContainerSet actionSet : taskManager.getAllActionSets(ctx.getHubContext())) {
            results.put(new PropertyContainerSetDTO(actionSet).toJSON(linkHelper));
        }
        return new JsonRepresentation(results);
    }
}

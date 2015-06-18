package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOHelper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
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
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/actionClasses Get all action classes
     * @apiVersion 0.5.0
     * @apiName GetAllActionClasses
     * @apiDescription Retrieves a list of all available action classes (regardless of plugin).
     * @apiGroup Tasks
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (the only supported value is "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/tasks/actionClasses",
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-actions/actionClasses/turnOn"
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-actions/actionClasses/log"
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        boolean expandItems = expansions.has("item");

        ItemListDTO results = new ItemListDTO(linkProvider.createTaskActionClassesLink(ctx.getHubContext()));
        for (PropertyContainerClass actionClass : taskManager.getAllActionClasses(ctx.getHubContext())) {
            PropertyContainerClassDTO.Builder builder = new PropertyContainerClassDTO.Builder(
                linkProvider.createTaskActionClassLink(actionClass.getContext())
            );
            if (expandItems) {
                builder.name(actionClass.getName())
                    .supportedProperties(DTOHelper.mapTypedPropertyList(actionClass.getSupportedProperties()));
            }
            results.add(builder.build());
        }
        return new JsonRepresentation(results.toJSON());
    }
}

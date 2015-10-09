/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.api.task.condition.ConditionClassType;
import com.whizzosoftware.hobson.api.task.condition.TaskConditionClass;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.task.TaskConditionClassDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
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
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/tasks/conditionClasses Get all condition classes
     * @apiVersion 0.5.0
     * @apiName GetAllConditionClasses
     * @apiDescription Retrieves a list of all available condition classes (regardless of plugin).
     * @apiGroup Tasks
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (the only supported value is "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/tasks/conditionClasses",
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-rules/conditionClasses/turnOff"
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule"
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
        boolean applyConstraints = Boolean.parseBoolean(getQueryValue("constraints"));
        ConditionClassType type = null;
        String s = getQueryValue("type");
        if (s != null) {
            type = ConditionClassType.valueOf(s);
        }

        ItemListDTO results = new ItemListDTO(linkProvider.createTaskConditionClassesLink(ctx.getHubContext()));
        for (TaskConditionClass conditionClass : taskManager.getAllConditionClasses(ctx.getHubContext(), type, applyConstraints)) {
            TaskConditionClassDTO.Builder builder = new TaskConditionClassDTO.Builder(
                    linkProvider.createTaskConditionClassLink(conditionClass.getContext())
            );
            if (expandItems) {
                builder.type(conditionClass.getConditionClassType().toString()).name(conditionClass.getName())
                        .descriptionTemplate(conditionClass.getDescriptionTemplate())
                        .supportedProperties(DTOMapper.mapTypedPropertyList(conditionClass.getSupportedProperties()));
            }
            results.add(builder.build());
        }
        return new JsonRepresentation(results.toJSON());
    }
}

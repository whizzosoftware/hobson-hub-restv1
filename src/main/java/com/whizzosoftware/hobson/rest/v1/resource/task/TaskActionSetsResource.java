/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.property.PropertyContainerSet;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerSetDTO;
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

public class TaskActionSetsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/tasks/actionSets";

    @Inject
    Authorizer authorizer;
    @Inject
    TaskManager taskManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/tasks/actionSets Get all action sets
     * @apiVersion 0.5.0
     * @apiName GetAllActionSets
     * @apiDescription Retrieves a summary list of all available action sets (regardless of plugin).
     * @apiGroup Tasks
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (the only supported value is "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/tasks/actionSets",
     *   "numberOfItems": 1,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/tasks/actionSets/dc419994-987f-4bff-81f9-e5bce53733f3"
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

        ItemListDTO results = new ItemListDTO(linkProvider.createTaskActionSetsLink(ctx.getHubContext()));
        boolean expandItems = expansions.has("item");

        for (PropertyContainerSet actionSet : taskManager.getAllActionSets(ctx.getHubContext())) {
            PropertyContainerSetDTO.Builder builder = new PropertyContainerSetDTO.Builder(
                linkProvider.createTaskActionSetLink(ctx.getHubContext(), actionSet.getId())
            );
            if (expandItems) {
                builder.containers(DTOHelper.mapPropertyContainerList(actionSet.getProperties()));
            }
            results.add(builder.build());
        }

        return new JsonRepresentation(results.toJSON());
    }
}

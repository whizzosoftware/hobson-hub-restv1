/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.action;

import com.whizzosoftware.hobson.api.action.ActionClass;
import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.action.ActionClassDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;

public class ActionClassesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/actionClasses";

    @Inject
    TaskManager taskManager;
    @Inject
    ActionManager actionManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/actionClasses Get all action classes
     * @apiVersion 0.5.0
     * @apiName GetAllActionClasses
     * @apiDescription Retrieves a list of all action classes published to the hub.
     * @apiGroup Hub
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
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        boolean expandItems = expansions.has("item");
        boolean applyConstraints = Boolean.parseBoolean(getQueryValue("constraints"));
        String id = getQueryValue("id");

        ItemListDTO results = new ItemListDTO(idProvider.createActionClassesId(ctx.getHubContext()));
        Collection<ActionClass> actionClasses = actionManager.getActionClasses(ctx.getHubContext(), applyConstraints);
        if (actionClasses != null) {
            for (ActionClass ac : actionClasses) {
                if (id == null || id.equals(ac.getContext().getContainerClassId())) {
                    ActionClassDTO.Builder builder = new ActionClassDTO.Builder(idProvider.createActionClassId(ac.getContext()), DTOMapper.mapTypedPropertyList(ac.getSupportedProperties()));
                    if (expandItems) {
                        builder.name(ac.getName()).descriptionTemplate(ac.getDescription()).taskAction(ac.isTaskAction());
                    }
                    results.add(builder.build());
                }
            }
        }

        return new JsonRepresentation(results.toJSON());
    }
}

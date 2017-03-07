/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.activity;

import com.whizzosoftware.hobson.api.activity.ActivityLogEntry;
import com.whizzosoftware.hobson.api.activity.ActivityLogManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.activity.ActivityEventDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource that manages a the hub activity log.
 *
 * @author Dan Noguerol
 */
public class ActivityLogResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/activityLog";

    @Inject
    AccessManager accessManager;
    @Inject
    ActivityLogManager activityManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/activityLog Get activity log
     * @apiVersion 0.1.3
     * @apiName GetActivityLog
     * @apiDescription Retrieves the most recent entries from the hub activity log.
     * @apiGroup Activities
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "timestamp": 1234,
     *         "name": "Thermostat temperature changed to 70",
     *     },
     *     {
     *       "timestamp": 1234,
     *       "name": "Light has turned on",
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        ItemListDTO dto = new ItemListDTO(bctx, bctx.getIdProvider().createActivityLogId(ctx.getHubContext()));
        for (ActivityLogEntry event : activityManager.getActivityLog(25)) {
            dto.add(new ActivityEventDTO(event.getName(), event.getTimestamp()));
        }

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }
}

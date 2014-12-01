/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.config;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource that manages the Hub admin password.
 *
 * @author Dan Noguerol
 */
public class HubPasswordResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/configuration/password";
    public static final String REL = "password";

    @Inject
    HubManager hubManager;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/configuration/password Set Hub password
     * @apiVersion 0.1.6
     * @apiName SetPassword
     * @apiDescription Sets the Hub password.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "currentPassword": "password1",
     *   "newPassword": "password2"
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        hubManager.setHubPassword(ctx.getUserId(), ctx.getHubId(), JSONMarshaller.createPasswordChange(JSONMarshaller.createJSONFromRepresentation(entity)));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

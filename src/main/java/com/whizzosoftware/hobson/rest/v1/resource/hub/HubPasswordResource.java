/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
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
    public static final String PATH = "/users/{userId}/hubs/{hubId}/password";
    public static final String REL = "password";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/password Set Hub password
     * @apiVersion 0.1.6
     * @apiName SetPassword
     * @apiDescription Sets the Hub password. The complexity requirements for the new password are: 1. Between 8 and 14 characters long, 2. At least one upper and lower case letter, 3. At least one number, 4. At least one special character.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "currentPassword": "password1",
     *   "newPassword": "password2"
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     * @apiErrorExample {json} Error Response:
     * HTTP/1.1 400 Bad Request
     * Content-Type: application/json; charset=UTF-8
     *
     * {"message":"New password does not meet complexity requirements"}
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        hubManager.setHubPassword(ctx.getUserId(), ctx.getHubId(), JSONSerializationHelper.createPasswordChange(JSONHelper.createJSONFromRepresentation(entity)));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.user;

import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import javax.inject.Inject;

public class UserResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}";

    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId Get User details
     * @apiVersion 0.5.0
     * @apiName GetUser
     * @apiDescription Retrieves details about the current user.
     * @apiGroup User
     * @apiSuccessExample Success Response:
     * {
     *   "id": "local",
     *   "firstName": "Local",
     *   "lastName": "User",
     *   "links": {
     *     "self": "/api/v1/users/local",
     *     "hubs": "/api/v1/users/local/hubs"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        User user = getRequest().getClientInfo().getUser();
        return new JsonRepresentation(
            linkHelper.addUserLinks(
                ctx,
                JSONSerializationHelper.createUserJSON(
                    new com.whizzosoftware.hobson.api.user.User.Builder().
                        id(user.getIdentifier()).
                        firstName(user.getFirstName()).
                        lastName(user.getLastName()).
                        email(user.getEmail()).
                        build()
                )
            )
        );
    }
}

/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.user;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.api.user.UserStore;
import com.whizzosoftware.hobson.dto.PersonDTO;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import javax.inject.Inject;

public class UserResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}";

    @Inject
    UserStore userStore;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId Get user details
     * @apiVersion 0.5.0
     * @apiName GetUser
     * @apiDescription Retrieves details about a user.
     * @apiGroup User
     * @apiSuccessExample Success Response:
     * {
     *   "firstName": "Local",
     *   "lastName": "User",
     *   "_links": {
     *     "self": {
     *       "href": "/api/v1/users/local"
     *     },
     *     "hubs": {
     *       "href": "/api/v1/users/local/hubs"
     *     }
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        User user = getRequest().getClientInfo().getUser();
        if (user != null) {
            if (user.getIdentifier().equals(ctx.getUserId())) {
                HobsonUser hu = userStore.getUser(user.getIdentifier());
                PersonDTO dto = new PersonDTO.Builder(linkProvider.createUserLink(hu.getId())).build();
                JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
                jr.setMediaType(new MediaType(dto.getMediaType() + "+json"));
                return jr;
            } else {
                throw new HobsonAuthorizationException("You are not authorized to access that information");
            }
        } else {
            throw new HobsonRuntimeException("No user information could be located");
        }
    }
}

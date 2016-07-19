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
import com.whizzosoftware.hobson.api.data.TelemetryManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.HobsonUserDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
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
    TelemetryManager telemetryManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {get} /api/v1/users/:userId Get user details
     * @apiVersion 0.5.0
     * @apiName GetUser
     * @apiDescription Retrieves details about a user.
     * @apiGroup User
     * @apiSuccessExample Success Response:
     * {
     *   "@id": "/api/v1/users/local",
     *   "firstName": "Local",
     *   "lastName": "User",
     *   "hubs": {
     *     "@id": "/api/v1/users/local/hubs"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        String userId = getAttribute("userId");

        User user = getRequest().getClientInfo().getUser();
        if (user != null && user instanceof HobsonRestUser) {
            if (user.getIdentifier().equals(userId)) {
                HobsonUserDTO dto = new HobsonUserDTO.Builder(
                    dtoBuildContextFactory.createContext(ctx.getApiRoot(), new ExpansionFields(getQueryValue("expand"))),
                    ((HobsonRestUser)user).getUser(),
                    telemetryManager != null && !telemetryManager.isStub(),
                    true
                ).build();

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

/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.user;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.data.DataStreamManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.HobsonUserDTO;
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
import org.restlet.security.User;

import javax.inject.Inject;

public class UserResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}";

    @Inject
    DataStreamManager dataStreamManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    AccessManager accessManager;

    @Override
    protected Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.USER_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        String userId = getAttribute("userId");

        User user = getRequest().getClientInfo().getUser();
        if (user != null && user instanceof HobsonRestUser) {
            if (user.getIdentifier().equals(userId)) {
                HobsonUserDTO dto = new HobsonUserDTO.Builder(
                    bctx,
                    ((HobsonRestUser)user).getUser(),
                    accessManager.getHubsForUser(userId),
                    true
                ).build();

                dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

                JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
                jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
                return jr;
            } else {
                throw new HobsonAuthorizationException("Forbidden");
            }
        } else {
            throw new HobsonRuntimeException("No user information could be located");
        }
    }
}

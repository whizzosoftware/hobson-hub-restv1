/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.user;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.HobsonUserDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import javax.inject.Inject;

public class UserInfoResource extends SelfInjectingServerResource {
    public static final String PATH = "/userInfo";

    @Inject
    AccessManager accessManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        User user = getRequest().getClientInfo().getUser();
        if (user != null && user instanceof HobsonRestUser) {
            HobsonUserDTO dto = new HobsonUserDTO.Builder(
                bctx,
                ((HobsonRestUser)user).getUser(),
                accessManager.getHubsForUser(user.getIdentifier()),
                true
            ).build();

            dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
            jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
            return jr;
        } else {
            throw new HobsonRuntimeException("No user information could be located");
        }
    }
}

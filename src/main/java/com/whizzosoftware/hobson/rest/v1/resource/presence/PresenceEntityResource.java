/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.event.EventManager;
import com.whizzosoftware.hobson.api.event.presence.PresenceUpdateRequestEvent;
import com.whizzosoftware.hobson.api.presence.PresenceEntity;
import com.whizzosoftware.hobson.api.presence.PresenceEntityContext;
import com.whizzosoftware.hobson.api.presence.PresenceLocation;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.presence.PresenceEntityDTO;
import com.whizzosoftware.hobson.dto.presence.PresenceLocationDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for retrieving a list of presence entities.
 *
 * @author Dan Noguerol
 */
public class PresenceEntityResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/presence/entities/{entityId}";

    @Inject
    AccessManager accessManager;
    @Inject
    PresenceManager presenceManager;
    @Inject
    EventManager eventManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PRESENCE_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        PresenceEntityContext pctx = PresenceEntityContext.create(ctx.getHubContext(), getAttribute("entityId"));
        PresenceEntity entity = presenceManager.getPresenceEntity(pctx);

        PresenceEntityDTO dto = new PresenceEntityDTO.Builder(
            bctx,
            entity,
            true
        ).build();

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation patch(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PRESENCE_UPDATE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        PresenceEntityContext pec = PresenceEntityContext.create(ctx.getHubContext(), getAttribute("entityId"));
        PresenceLocationDTO dto = new PresenceLocationDTO.Builder(json.getJSONObject("location")).build();

        PresenceLocation loc = DTOMapper.mapPresenceLocationDTO(dto);
        eventManager.postEvent(ctx.getHubContext(), new PresenceUpdateRequestEvent(System.currentTimeMillis(), pec, loc != null ? loc.getContext() : null));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    @Override
    protected Representation delete() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PRESENCE_DELETE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        PresenceEntityContext pec = PresenceEntityContext.create(ctx.getHubContext(), getAttribute("entityId"));
        presenceManager.deletePresenceEntity(pec);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

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

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.presence.PresenceEntity;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.presence.PresenceEntityDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
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
 * A REST resource for adding and retrieving presence entities.
 *
 * @author Dan Noguerol
 */
public class PresenceEntitiesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/presence/entities";
    public static final String TEMPLATE = "/hubs/{hubId}/presence/{presenceType}";

    @Inject
    AccessManager accessManager;
    @Inject
    PresenceManager presenceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PRESENCE_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        ItemListDTO dto = new ItemListDTO(bctx, idProvider.createPresenceEntitiesId(ctx.getHubContext()), true);
        boolean showDetails = expansions.has(JSONAttributes.ITEM);
        expansions.pushContext(JSONAttributes.ITEM);
        for (PresenceEntity entity : presenceManager.getAllPresenceEntities(ctx.getHubContext())) {
            dto.add(new PresenceEntityDTO.Builder(
                bctx,
                entity,
                showDetails
            ).build());
        }
        expansions.popContext();

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation post(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PRESENCE_UPDATE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
        if (json.has("name") && json.getString("name").trim().length() > 0) {
            presenceManager.addPresenceEntity(ctx.getHubContext(), json.getString("name"));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("A name is required");
        }
    }

    @Override
    protected Representation delete() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PRESENCE_DELETE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        for (PresenceEntity entity : presenceManager.getAllPresenceEntities(ctx.getHubContext())) {
            presenceManager.deletePresenceEntity(entity.getContext());
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.action;

import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.property.PropertyContainerSetDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class ActionSetsResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/actionSets";

    @Inject
    AccessManager accessManager;
    @Inject
    ActionManager actionManager;
    @Inject
    IdProvider idProvider;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_READ, null);

        ItemListDTO dto = new ItemListDTO(bctx, idProvider.createTaskActionSetsId(ctx.getHubContext()));
        boolean expandItems = expansions.has("item");

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return actionManager.getActionClass(ctx);
            }
        };

        for (PropertyContainerSet actionSet : actionManager.getActionSets(ctx.getHubContext())) {
            PropertyContainerSetDTO.Builder builder = new PropertyContainerSetDTO.Builder(
                bctx, idProvider.createTaskActionSetId(ctx.getHubContext(), actionSet.getId())
            );
            if (expandItems) {
                builder.containers(DTOMapper.mapPropertyContainerList(bctx, actionSet.getProperties(), PropertyContainerClassType.ACTION, false, pccp));
            }
            dto.add(builder.build());
        }

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }
}

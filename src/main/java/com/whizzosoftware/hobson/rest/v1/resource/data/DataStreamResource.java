/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.data;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.data.DataStreamManager;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.data.DataStreamDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class DataStreamResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/dataStreams/{dataStreamId}";
    public static final String TEMPLATE = "/hubs/{hubId}/dataStreams/{dataStreamId}";

    @Inject
    DataStreamManager dataStreamManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() {
        if (dataStreamManager != null && !dataStreamManager.isStub()) {
            HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
            ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
            DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);
            DataStreamDTO dto = new DataStreamDTO.Builder(
                bctx,
                ctx.getHubContext(),
                dataStreamManager.getDataStream(ctx.getHubContext(), getAttribute("dataStreamId")),
                true
            ).build();

            dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
            jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
            return jr;
        } else {
            throw new HobsonNotFoundException("No data stream manager is available");
        }
    }

    @Override
    protected Representation delete() {
        if (!isInRole(HobsonRole.administrator.name()) && !isInRole(HobsonRole.userWrite.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }
        if (dataStreamManager != null && !dataStreamManager.isStub()) {
            HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
            dataStreamManager.deleteDataStream(ctx.getHubContext(), getAttribute("dataStreamId"));
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("No data stream manager is available");
        }
    }
}

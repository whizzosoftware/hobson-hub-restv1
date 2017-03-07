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

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.data.DataStreamManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.data.DataStreamDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
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
    AccessManager accessManager;
    @Inject
    DataStreamManager dataStreamManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() {
        if (dataStreamManager != null && !dataStreamManager.isStub()) {
            final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
            final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
            final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

            accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DATASTREAM_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

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
        if (dataStreamManager != null && !dataStreamManager.isStub()) {
            final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

            accessManager.authorize(null, AuthorizationAction.DATASTREAM_DELETE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

            dataStreamManager.deleteDataStream(ctx.getHubContext(), getAttribute("dataStreamId"));
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("No data stream manager is available");
        }
    }
}

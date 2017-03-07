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
import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.data.DataStream;
import com.whizzosoftware.hobson.api.data.DataStreamManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.data.DataStreamDTO;
import com.whizzosoftware.hobson.dto.data.DataStreamFieldDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class DataStreamsResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/dataStreams";
    public static final String TEMPLATE = "/hubs/{hubId}/{entity}";

    @Inject
    AccessManager accessManager;
    @Inject
    DataStreamManager dataStreamManager;
    @Inject
    IdProvider idProvider;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DATASTREAM_READ, null);

        ItemListDTO dto = new ItemListDTO(bctx, bctx.getIdProvider().createDataStreamsId(ctx.getHubContext()));
        if (dataStreamManager != null && !dataStreamManager.isStub()) {
            for (DataStream ds : dataStreamManager.getDataStreams(ctx.getHubContext())) { // TODO
                dto.add(new DataStreamDTO.Builder(bctx, ctx.getHubContext(), ds, expansions.has(JSONAttributes.ITEM)).build());
            }
        }

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation post(Representation entity) {
        if (dataStreamManager != null && !dataStreamManager.isStub()) {
            final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
            final DataStreamDTO dto = new DataStreamDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();

            accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DATASTREAM_CREATE, null);

            List<DataStreamField> fields = new ArrayList<>();
            for (DataStreamFieldDTO v : dto.getFields()) {
                fields.add(new DataStreamField(v.getId(), v.getName(), idProvider.createDeviceVariableContext(v.getVariable().getId())));
            }

            dataStreamManager.createDataStream(ctx.getHubContext(), dto.getName(), fields, null);

            getResponse().setStatus(Status.SUCCESS_CREATED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("No data stream manager is available");
        }
    }
}

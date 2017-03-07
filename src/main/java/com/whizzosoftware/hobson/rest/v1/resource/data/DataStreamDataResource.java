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

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.data.DataStream;
import com.whizzosoftware.hobson.api.persist.PropertyConstants;
import com.whizzosoftware.hobson.api.data.DataStreamInterval;
import com.whizzosoftware.hobson.api.data.DataStreamManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.data.DataStreamDataDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class DataStreamDataResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/dataStreams/{dataStreamId}/data";
    public static final String TEMPLATE = "/hubs/{hubId}/dataStreams/{dataStreamId}/data";

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

            String dataStreamId = getAttribute(PropertyConstants.DATA_STREAM_ID);

            long endTime;
            String s = getQueryValue("endTime");
            if (s != null) {
                endTime = Long.parseLong(s);
            } else {
                endTime = System.currentTimeMillis();
            }

            DataStreamInterval inr;
            s = getQueryValue("inr");
            if (s != null) {
                inr = DataStreamInterval.valueOf(s);
            } else {
                inr = DataStreamInterval.HOURS_1;
            }

            DataStream ds = dataStreamManager.getDataStream(ctx.getHubContext(), dataStreamId);

            DataStreamDataDTO dto = new DataStreamDataDTO.Builder(bctx, ctx.getHubContext(), dataStreamId, endTime, inr).
                fields(ds.getFields()).
                data(dataStreamManager.getData(ctx.getHubContext(), dataStreamId, endTime, inr)).
                build();

            dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
            jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
            return jr;
        } else {
            throw new HobsonNotFoundException("No data stream manager is available");
        }
    }
}

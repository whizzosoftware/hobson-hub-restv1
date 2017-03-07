/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.hub.LineRange;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.MediaTypes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.Header;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import javax.inject.Inject;
import java.io.IOException;

/**
 * A REST resource for retrieving content from the Hub log.
 *
 * @author Dan Noguerol
 */
public class HubLogResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/log";
    public static final String TEMPLATE = "/hubs/{hubId}/{entity}";

    @Inject
    AccessManager accessManager;
    @Inject
    HubManager hubManager;

    @Override
    protected Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        long startLine = 0;
        long endLine = 24;

        // since we are using a custom range, we can't use the standard Restlet range mechanism
        Series<Header> headers = getRequest().getHeaders();
        String rangeStr = headers.getFirstValue("Range");
        if (rangeStr != null) {
            LineRange range = new LineRange(rangeStr);
            startLine = range.hasStartLine() ? range.getStartLine(): 0;
            endLine = range.hasEndLine() ? range.getEndLine() : Long.MAX_VALUE - 1;
        }

        AppendableRepresentation ar = new AppendableRepresentation();
        ar.setMediaType(MediaTypeHelper.createMediaType(getRequest(), MediaTypes.ITEM_LIST));

        try {
            ar.append("{\"@id\":\"").append(ctx.getApiRoot()).append(HubLogResource.PATH).append("\",\"itemListElement\":");

            LineRange lineRange = hubManager.getLog(ctx.getHubContext(), startLine, endLine, ar);

            ar.append(", \"numberOfItems\": \"").append(Long.toString(lineRange.getLineCount() + 1)).append("\"}");

            Range range = new Range(lineRange.getStartLine(), lineRange.getLineCount() > 0 ? lineRange.getLineCount() : 0);
            range.setUnitName("lines");
            ar.setRange(range);

            getResponse().setStatus(Status.SUCCESS_OK);

            return ar;
        } catch (IOException e) {
            throw new HobsonRuntimeException("An error occurred creating JSON response", e);
        }
    }
}

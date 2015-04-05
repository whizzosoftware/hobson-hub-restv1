/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.hub.LineRange;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import javax.inject.Inject;
import java.util.Map;

/**
 * A REST resource for retrieving content from the Hub log.
 *
 * @author Dan Noguerol
 */
public class LogResource extends SelfInjectingServerResource {
    private final static String HEADERS = "org.restlet.http.headers";

    public static final String PATH = "/users/{userId}/hubs/{hubId}/log";
    public static final String REL = "log";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;

    /**
     * @api {get} /api/v1/users/local/hubs/local/log Get log content
     * @apiHeader {String} Range the byte range to return in RFC 7233 format
     * @apiVersion 0.1.7
     * @apiName GetLog
     * @apiDescription Retrieves content from the Hub log file.
     * @apiGroup Logging
     * @apiExample Example request:
     * GET /api/v1/users/local/hubs/local/log HTTP/1.1
     * Range: lines=0-100
     * @apiSuccessExample Success Response:
     * HTTP/1.1 206 Partial Content
     * Content-Range: lines 0-100/5280
     * ... Log data ...
     */
    @Override
    protected Representation get() throws ResourceException {
        long startLine = 0;
        long endLine = 24;

        // since we are using a custom range, we can't use the standard Restlet range mechanism
        Series<Header> headers = (Series<Header>)getRequest().getAttributes().get(HEADERS);
        String rangeStr = headers.getFirstValue("Range");
        if (rangeStr != null) {
            LineRange range = new LineRange(rangeStr);
            startLine = range.hasStartLine() ? range.getStartLine(): 0;
            endLine = range.hasEndLine() ? range.getEndLine() : Long.MAX_VALUE - 1;
        }

        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());

        AppendableRepresentation ar = new AppendableRepresentation();
        ar.setMediaType(MediaType.APPLICATION_JSON);
        LineRange lineRange = hubManager.getLog(ctx.getHubContext(), startLine, endLine, ar);

        Map<String,Object> attrs = getResponse().getAttributes();
        headers = (Series<Header>)attrs.get(HEADERS);
        if (headers == null) {
            headers = new Series<>(Header.class);
            attrs.put(HEADERS, headers);
        }
        headers.add(new Header("Range", lineRange.toString()));

        getResponse().setStatus(Status.SUCCESS_PARTIAL_CONTENT);

        return ar;
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource for retrieving content from the Hub log.
 *
 * @author Dan Noguerol
 */
public class LogResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/log";
    public static final String REL = "log";

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
     * GET /api/v1/log HTTP/1.1
     * Range: bytes=0-100
     * @apiSuccessExample Success Response:
     * HTTP/1.1 206 Partial Content
     * Content-Range: bytes 0-100/65000
     * Content-Length: 101
     * ... Log data ...
     */
    @Override
    protected Representation get() throws ResourceException {
        int startIndex = 0;
        int endIndex = 40000;
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new ByteArrayRepresentation(hubManager.getLog(ctx.getUserId(), ctx.getHubId(), startIndex, endIndex).getBytes());
    }
}

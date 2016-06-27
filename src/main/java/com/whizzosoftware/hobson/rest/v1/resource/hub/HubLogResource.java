/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.hub.LineRange;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
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

    @Inject
    HubManager hubManager;

    /**
     * @api {get} /api/v1/users/local/hubs/local/log Get Hub log
     * @apiHeader {String} Range the byte range to return in RFC 7233 format
     * @apiVersion 0.1.7
     * @apiName GetHubLog
     * @apiDescription Retrieves content from the Hub log file.
     * @apiGroup Hub
     * @apiExample Example request:
     * GET /api/v1/users/local/hubs/local/log HTTP/1.1
     * Range: lines=0-100
     * @apiSuccessExample Success Response:
     * {
     *   "numberOfItems": "24",
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "time": "1434537854597",
     *         "thread": "qtp2146976730-31",
     *         "level": "INFO",
     *         "message": "Initialized jose4j in 205ms"
     *       }
     *     },
     *     {
     *       "item": {
     *         "time": "1434537854597",
     *         "thread": "qtp2146976730-31",
     *         "level": "INFO",
     *         "message": "JWE compression algorithms: [DEF]"
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
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

        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        AppendableRepresentation ar = new AppendableRepresentation();
        ar.setMediaType(MediaType.APPLICATION_JSON);

        try {
            ar.append("{\"itemListElement\":");

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

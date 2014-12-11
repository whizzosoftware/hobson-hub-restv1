/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import org.json.JSONException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.StatusService;

/**
 * Restlet StatusService implementation for returning JSON formatted error messages.
 *
 * @author Dan Noguerol
 */
public class HobsonStatusService extends StatusService {
    public Status getStatus(Throwable t, Request request, Response response) {
        if (t instanceof HobsonNotFoundException) {
            return new Status(Status.CLIENT_ERROR_NOT_FOUND, t);
        } else if (t instanceof JSONException) {
            return new Status(Status.CLIENT_ERROR_BAD_REQUEST, t);
        } else {
            return new Status(Status.SERVER_ERROR_INTERNAL, t);
        }
    }

    public Representation getRepresentation(Status status, Request request, Response response) {
        if (status != null) {
            return new JsonRepresentation(JSONMarshaller.createErrorJSON(status.getThrowable()));
        } else {
            return new JsonRepresentation(JSONMarshaller.createErrorJSON("An unknown error has occurred"));
        }
    }
}

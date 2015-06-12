/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.*;
import com.whizzosoftware.hobson.dto.ErrorsDTO;
import org.json.JSONException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restlet StatusService implementation for returning JSON formatted error messages.
 *
 * @author Dan Noguerol
 */
public class HobsonStatusService extends StatusService {
    private static final Logger logger = LoggerFactory.getLogger(HobsonStatusService.class);

    public Status getStatus(Throwable t, Request request, Response response) {
        if (t instanceof HobsonNotFoundException) {
            if (t.getLocalizedMessage() != null) {
                return new Status(Status.CLIENT_ERROR_NOT_FOUND, t.getLocalizedMessage());
            } else {
                return new Status(Status.CLIENT_ERROR_NOT_FOUND, t);
            }
        } else if (t instanceof HobsonAuthenticationException) {
            return new Status(Status.CLIENT_ERROR_UNAUTHORIZED, t.getLocalizedMessage());
        } else if (t instanceof HobsonAuthorizationException) {
            return new Status(Status.CLIENT_ERROR_FORBIDDEN, t.getLocalizedMessage());
        } else if (t instanceof JSONException || t instanceof HobsonInvalidRequestException) {
            return new Status(Status.CLIENT_ERROR_BAD_REQUEST, t.getLocalizedMessage());
        } else if (t instanceof HobsonRuntimeException) {
            return new Status(Status.SERVER_ERROR_INTERNAL, t, t.getLocalizedMessage());
        } else {
            return new Status(Status.SERVER_ERROR_INTERNAL, t);
        }
    }

    public Representation getRepresentation(Status status, Request request, Response response) {
        Integer code = (status != null) ? status.getCode() : null;
        if (status != null && status.getDescription() != null) {
            return new JsonRepresentation(new ErrorsDTO.Builder(code, status.getDescription()).build().toJSON());
        } else if (status != null && status.getThrowable() != null) {
            return new JsonRepresentation(new ErrorsDTO.Builder(status.getThrowable()).build().toJSON());
        } else {
            logger.error("Unknown error servicing request: " + request.getOriginalRef());
            return new JsonRepresentation(new ErrorsDTO.Builder(code, "An unknown error has occurred").build().toJSON());
        }
    }
}

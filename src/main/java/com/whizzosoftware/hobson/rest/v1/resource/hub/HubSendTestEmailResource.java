/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A resource that sends test e-mails.
 *
 * @author Dan Noguerol
 */
public class HubSendTestEmailResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/configuration/sendTestEmail";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/configuration/sendTestEmail Send test e-mail
     * @apiVersion 0.5.0
     * @apiName SendTestEmail
     * @apiDescription Sends a test e-mail message using provided e-mail account details.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "server": "smtp.mydomain.com",
     *   "secure": true,
     *   "senderAddress": "foo@bar.com",
     *   "username": "user",
     *   "password": "password"
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     * @apiErrorExample {json} Error Response:
     * HTTP/1.1 400 Bad Request
     * Content-Type: application/json; charset=UTF-8
     *
     * {"message":"Unable to send test e-mail with provided account information"}
     */
    @Override
    protected Representation post(Representation entity) throws ResourceException {
        try {
            HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
            authorizer.authorizeHub(ctx.getHubContext());
            hubManager.sendTestEmail(ctx.getHubContext(), JSONSerializationHelper.createEmailConfiguration(JSONHelper.createJSONFromRepresentation(entity)));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } catch (HobsonRuntimeException e) {
            throw new HobsonInvalidRequestException("Unable to send test e-mail with provided account information", e);
        }
    }
}

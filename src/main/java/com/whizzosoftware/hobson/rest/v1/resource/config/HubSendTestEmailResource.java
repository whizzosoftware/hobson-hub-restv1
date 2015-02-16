package com.whizzosoftware.hobson.rest.v1.resource.config;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class HubSendTestEmailResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/configuration/sendTestEmail";

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
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        hubManager.sendTestEmail(ctx.getUserId(), ctx.getHubId(), JSONMarshaller.createEmailConfiguration(JSONMarshaller.createJSONFromRepresentation(entity)));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

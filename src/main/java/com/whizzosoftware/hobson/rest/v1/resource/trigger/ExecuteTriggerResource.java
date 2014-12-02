package com.whizzosoftware.hobson.rest.v1.resource.trigger;

import com.whizzosoftware.hobson.api.trigger.TriggerManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class ExecuteTriggerResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/triggers/{providerId}/{triggerId}/execute";

    @Inject
    TriggerManager triggerManager;

    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        triggerManager.executeTrigger(ctx.getUserId(), ctx.getHubId(), getAttribute("providerId"), getAttribute("triggerId"));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

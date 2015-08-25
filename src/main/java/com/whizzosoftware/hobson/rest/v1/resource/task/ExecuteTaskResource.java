package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.task.TaskContext;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class ExecuteTaskResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/tasks/{taskId}/execute";

    @Inject
    Authorizer authorizer;
    @Inject
    TaskManager taskManager;

    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        taskManager.fireTaskTrigger(TaskContext.create(ctx.getHubContext(), getAttribute("taskId")));
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}

package com.whizzosoftware.hobson.rest.v1.resource.hub;

import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class HubConfigurationClassResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/configurationClass";

    @Override
    protected Representation get() throws ResourceException {
        return new EmptyRepresentation();
    }
}

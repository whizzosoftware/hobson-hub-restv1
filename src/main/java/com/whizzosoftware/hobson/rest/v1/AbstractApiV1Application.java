/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

import com.whizzosoftware.hobson.rest.BearerTokenVerifier;
import com.whizzosoftware.hobson.rest.HobsonApiApplication;
import com.whizzosoftware.hobson.rest.HobsonStatusService;
import com.whizzosoftware.hobson.rest.HobsonRole;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.ShutdownResource;
import com.whizzosoftware.hobson.rest.v1.resource.login.LoginResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceLocationResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceLocationsResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntityResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.*;
import com.whizzosoftware.hobson.rest.v1.resource.activity.ActivityLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.hub.*;
import com.whizzosoftware.hobson.rest.v1.resource.image.HubImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryGroupResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryRootResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.user.UserResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariableResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariablesResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.ext.guice.ResourceInjectingApplication;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.ChallengeAuthenticator;

import java.util.ArrayList;

/**
 * The Hobson REST API v1.
 *
 * @author Dan Noguerol
 */
abstract public class AbstractApiV1Application extends ResourceInjectingApplication implements HobsonApiApplication {
    public static final String API_ROOT = "/api/v1";

    /**
     * Constructor that creates an challenge-based authenticator using the fully-qualified class name specified in
     * the "hobson.rest.verifier" system property to instantiate a verifier.
     */
    public AbstractApiV1Application() {
        super();

        setStatusService(new HobsonStatusService());

        // set up application roles
        for (HobsonRole r : HobsonRole.values()) {
            getRoles().add(r.value());
        }
    }

    @Override
    public Restlet createInboundRoot() {
        // create the secure router
        Router secureRouter = newRouter();
        secureRouter.attach(ActivityLogResource.PATH, ActivityLogResource.class);
        secureRouter.attach(DevicePassportsResource.PATH, DevicePassportsResource.class);
        secureRouter.attach(DevicePassportResource.PATH, DevicePassportResource.class);
        secureRouter.attach(DeviceResource.PATH, DeviceResource.class);
        secureRouter.attach(DeviceConfigurationResource.PATH, DeviceConfigurationResource.class);
        secureRouter.attach(DeviceConfigurationClassResource.PATH, DeviceConfigurationClassResource.class);
        secureRouter.attach(DeviceTelemetryResource.PATH, DeviceTelemetryResource.class);
        secureRouter.attach(DeviceTelemetryDatasetResource.PATH, DeviceTelemetryDatasetResource.class);
        secureRouter.attach(DeviceTelemetryDatasetsResource.PATH, DeviceTelemetryDatasetsResource.class);
        secureRouter.attach(DevicesResource.PATH, DevicesResource.class);
        secureRouter.attach(DeviceVariableResource.PATH, DeviceVariableResource.class);
        secureRouter.attach(DeviceVariablesResource.PATH, DeviceVariablesResource.class);
        secureRouter.attach(ExecuteTaskResource.PATH, ExecuteTaskResource.class);
        secureRouter.attach(GlobalVariableResource.PATH, GlobalVariableResource.class);
        secureRouter.attach(GlobalVariablesResource.PATH, GlobalVariablesResource.class);
        secureRouter.attach(HubConfigurationResource.PATH, HubConfigurationResource.class);
        secureRouter.attach(HubConfigurationClassResource.PATH, HubConfigurationClassResource.class);
        secureRouter.attach(HubImageResource.PATH, HubImageResource.class);
        secureRouter.attach(HubResource.PATH, HubResource.class);
        secureRouter.attach(HubPasswordResource.PATH, HubPasswordResource.class);
        secureRouter.attach(HubSendTestEmailResource.PATH, HubSendTestEmailResource.class);
        secureRouter.attach(HubRemoteRepositoriesResource.PATH, HubRemoteRepositoriesResource.class);
        secureRouter.attach(HubRemoteRepositoryResource.PATH, HubRemoteRepositoryResource.class);
        secureRouter.attach(HubsResource.PATH, HubsResource.class);
        secureRouter.attach(ImageLibraryGroupResource.PATH, ImageLibraryGroupResource.class);
        secureRouter.attach(ImageLibraryImageResource.PATH, ImageLibraryImageResource.class);
        secureRouter.attach(ImageLibraryRootResource.PATH, ImageLibraryRootResource.class);
        secureRouter.attach(LocalPluginsResource.PATH, LocalPluginsResource.class);
        secureRouter.attach(LocalPluginResource.PATH, LocalPluginResource.class);
        secureRouter.attach(LocalPluginConfigurationResource.PATH, LocalPluginConfigurationResource.class);
        secureRouter.attach(LocalPluginConfigurationClassResource.PATH, LocalPluginConfigurationClassResource.class);
        secureRouter.attach(LocalPluginImageResource.PATH, LocalPluginImageResource.class);
        secureRouter.attach(LocalPluginReloadResource.PATH, LocalPluginReloadResource.class);
        secureRouter.attach(HubLogResource.PATH, HubLogResource.class);
        secureRouter.attach(MediaProxyResource.PATH, MediaProxyResource.class);
        secureRouter.attach(PluginDevicesResource.PATH, PluginDevicesResource.class);
        secureRouter.attach(PresenceEntitiesResource.PATH, PresenceEntitiesResource.class);
        secureRouter.attach(PresenceEntityResource.PATH, PresenceEntityResource.class);
        secureRouter.attach(PresenceLocationsResource.PATH, PresenceLocationsResource.class);
        secureRouter.attach(PresenceLocationResource.PATH, PresenceLocationResource.class);
        secureRouter.attach(ActivateDevicePassportResource.PATH, ActivateDevicePassportResource.class);
        secureRouter.attach(RemotePluginsResource.PATH, RemotePluginsResource.class);
        secureRouter.attach(RemotePluginResource.PATH, RemotePluginResource.class);
        secureRouter.attach(RemotePluginInstallResource.PATH, RemotePluginInstallResource.class);
        secureRouter.attach(ShutdownResource.PATH, ShutdownResource.class);
        secureRouter.attach(TaskActionClassesResource.PATH, TaskActionClassesResource.class);
        secureRouter.attach(TaskActionSetsResource.PATH, TaskActionSetsResource.class);
        secureRouter.attach(TaskConditionClassesResource.PATH, TaskConditionClassesResource.class);
        secureRouter.attach(TaskResource.PATH, TaskResource.class);
        secureRouter.attach(TasksResource.PATH, TasksResource.class);
        secureRouter.attach(UserResource.PATH, UserResource.class);

        // create the authorizer
        Authorizer authorizer = createAuthorizer();
        authorizer.setNext(secureRouter);

        // create bearer token challenge authenticator
        ChallengeAuthenticator auth = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_OAUTH_BEARER, getRealmName());
        auth.setVerifier(new BearerTokenVerifier(this));
        auth.setNext(authorizer);

        // create the insecure router
        Router insecureRouter = newRouter();
        insecureRouter.attach(LoginResource.PATH, LoginResource.class);
        insecureRouter.attachDefault(auth);

        // allow subclasses to create any additional resources they need to
        createAdditionalResources(secureRouter, insecureRouter);

        // create a filter that prevents caching of API responses
        return new Filter(getContext(), insecureRouter) {
            protected void afterHandle(Request request, Response response) {
                super.afterHandle(request, response);
                if (response != null && response.getEntity() != null) {
                    if (response.getStatus().equals(Status.SUCCESS_OK)) {
                        response.setCacheDirectives(new ArrayList<CacheDirective>());
                        response.getCacheDirectives().add(CacheDirective.noCache());
                        response.getCacheDirectives().add(CacheDirective.noStore());
                    }
                }
            }
        };
    }

    public String getApiRoot() {
        return API_ROOT;
    }

    abstract protected String getRealmName();
    abstract protected Authorizer createAuthorizer();
    abstract protected void createAdditionalResources(Router secureRouter, Router insecureRouter);
}

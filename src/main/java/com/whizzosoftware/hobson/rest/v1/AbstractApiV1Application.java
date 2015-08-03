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
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.ShutdownResource;
import com.whizzosoftware.hobson.rest.v1.resource.login.LoginResource;
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
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.ext.guice.ResourceInjectingApplication;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

import java.util.ArrayList;

/**
 * The Hobson REST API v1.
 *
 * @author Dan Noguerol
 */
abstract public class AbstractApiV1Application extends ResourceInjectingApplication implements HobsonApiApplication {
    /**
     * Constructor that creates an challenge-based authenticator using the fully-qualified class name specified in
     * the "hobson.rest.verifier" system property to instantiate a verifier.
     */
    public AbstractApiV1Application() {
        super();
        setStatusService(new HobsonStatusService());
    }

    @Override
    public Restlet createInboundRoot() {
        // create the router with all of our resource classes attached
        Router router = newRouter();
        router.attach(ActivityLogResource.PATH, ActivityLogResource.class);
        router.attach(DeviceResource.PATH, DeviceResource.class);
        router.attach(DeviceConfigurationResource.PATH, DeviceConfigurationResource.class);
        router.attach(DeviceConfigurationClassResource.PATH, DeviceConfigurationClassResource.class);
        router.attach(DeviceTelemetryResource.PATH, DeviceTelemetryResource.class);
        router.attach(DeviceTelemetryDatasetResource.PATH, DeviceTelemetryDatasetResource.class);
        router.attach(DeviceTelemetryDatasetsResource.PATH, DeviceTelemetryDatasetsResource.class);
        router.attach(DevicesResource.PATH, DevicesResource.class);
        router.attach(DeviceVariableResource.PATH, DeviceVariableResource.class);
        router.attach(DeviceVariablesResource.PATH, DeviceVariablesResource.class);
        router.attach(ExecuteTaskResource.PATH, ExecuteTaskResource.class);
        router.attach(GlobalVariableResource.PATH, GlobalVariableResource.class);
        router.attach(GlobalVariablesResource.PATH, GlobalVariablesResource.class);
        router.attach(HubConfigurationResource.PATH, HubConfigurationResource.class);
        router.attach(HubConfigurationClassResource.PATH, HubConfigurationClassResource.class);
        router.attach(HubImageResource.PATH, HubImageResource.class);
        router.attach(HubResource.PATH, HubResource.class);
        router.attach(HubPasswordResource.PATH, HubPasswordResource.class);
        router.attach(HubSendTestEmailResource.PATH, HubSendTestEmailResource.class);
        router.attach(HubRemoteRepositoriesResource.PATH, HubRemoteRepositoriesResource.class);
        router.attach(HubRemoteRepositoryResource.PATH, HubRemoteRepositoryResource.class);
        router.attach(HubsResource.PATH, HubsResource.class);
        router.attach(ImageLibraryGroupResource.PATH, ImageLibraryGroupResource.class);
        router.attach(ImageLibraryImageResource.PATH, ImageLibraryImageResource.class);
        router.attach(ImageLibraryRootResource.PATH, ImageLibraryRootResource.class);
        router.attach(LocalPluginsResource.PATH, LocalPluginsResource.class);
        router.attach(LocalPluginResource.PATH, LocalPluginResource.class);
        router.attach(LocalPluginConfigurationResource.PATH, LocalPluginConfigurationResource.class);
        router.attach(LocalPluginConfigurationClassResource.PATH, LocalPluginConfigurationClassResource.class);
        router.attach(LocalPluginImageResource.PATH, LocalPluginImageResource.class);
        router.attach(LocalPluginReloadResource.PATH, LocalPluginReloadResource.class);
        router.attach(HubLogResource.PATH, HubLogResource.class);
        router.attach(LoginResource.PATH, LoginResource.class);
        router.attach(MediaProxyResource.PATH, MediaProxyResource.class);
        router.attach(PluginDevicesResource.PATH, PluginDevicesResource.class);
        router.attach(PresenceEntitiesResource.PATH, PresenceEntitiesResource.class);
        router.attach(RemotePluginsResource.PATH, RemotePluginsResource.class);
        router.attach(RemotePluginResource.PATH, RemotePluginResource.class);
        router.attach(RemotePluginInstallResource.PATH, RemotePluginInstallResource.class);
        router.attach(ShutdownResource.PATH, ShutdownResource.class);
        router.attach(TaskActionClassesResource.PATH, TaskActionClassesResource.class);
        router.attach(TaskActionSetsResource.PATH, TaskActionSetsResource.class);
        router.attach(TaskConditionClassesResource.PATH, TaskConditionClassesResource.class);
        router.attach(TaskResource.PATH, TaskResource.class);
        router.attach(TasksResource.PATH, TasksResource.class);
        router.attach(UserResource.PATH, UserResource.class);

        createAdditionalResources(router);

        // create a filter that prevents caching of API responses
        Filter cache = new Filter(getContext(), router) {
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

        // create bearer token challenge authenticator
        ChallengeAuthenticator auth = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_OAUTH_BEARER, getRealmName());
        auth.setVerifier(new BearerTokenVerifier());
        auth.setNext(cache);

        // make the authenticator the first restlet in the chain
        return auth;
    }

    public String getApiRoot() {
        return LinkProvider.API_ROOT;
    }

    abstract protected String getRealmName();
    abstract protected void createAdditionalResources(Router router);
}

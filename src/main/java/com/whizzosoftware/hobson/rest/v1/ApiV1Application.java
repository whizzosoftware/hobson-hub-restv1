/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

import com.whizzosoftware.hobson.rest.v1.resource.HubInfoResource;
import com.whizzosoftware.hobson.rest.v1.resource.LogResource;
import com.whizzosoftware.hobson.rest.v1.resource.ShutdownResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionsResource;
import com.whizzosoftware.hobson.rest.v1.resource.config.HubConfigurationResource;
import com.whizzosoftware.hobson.rest.v1.resource.config.HubPasswordResource;
import com.whizzosoftware.hobson.rest.v1.resource.config.HubSendTestEmailResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.image.HubImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryGroupResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryRootResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.ExecuteTaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TasksResource;
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
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * The Hobson REST API v1.
 *
 * @author Dan Noguerol
 */
public class ApiV1Application extends ResourceInjectingApplication implements HobsonApiApplication {
    private static final Logger logger = LoggerFactory.getLogger(ApiV1Application.class);
    private static final String PROP_VERIFIER = "hobson.rest.verifier";

    public static final String PATH = "/api/v1";

    private ChallengeAuthenticator authenticator;

    /**
     * Constructor that creates an challenge-based authenticator using the fully-qualified class name specified in
     * the "hobson.rest.verifier" system property to instantiate a verifier.
     */
    public ApiV1Application() {
        super();

        String verifierClassName = System.getProperty(PROP_VERIFIER);
        if (verifierClassName != null) {
            try {
                this.authenticator = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "Hobson (default is admin/admin)");
                this.authenticator.setVerifier((Verifier)Class.forName(verifierClassName).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                logger.error("Error creating REST API authenticator", e);
            }
        } else {
            logger.warn("No REST API authenticator has been configured!");
        }

        setStatusService(new HobsonStatusService());
    }

    /**
     * Constructor.
     *
     * @param verifier the authenticator verifier to use
     */
    public ApiV1Application(Verifier verifier) {
        super();
        this.authenticator = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "Hobson (default is admin/admin)");
        this.authenticator.setVerifier(verifier);
        setStatusService(new HobsonStatusService());
    }

    @Override
    public Restlet createInboundRoot() {
        // create the router with all of our resource classes attached
        Router router = newRouter();
        router.attach(ActionResource.PATH, ActionResource.class);
        router.attach(ActionsResource.PATH, ActionsResource.class);
        router.attach(HubInfoResource.PATH, HubInfoResource.class);
        router.attach(DeviceResource.PATH, DeviceResource.class);
        router.attach(DeviceConfigurationResource.PATH, DeviceConfigurationResource.class);
        router.attach(DeviceTelemetryResource.PATH, DeviceTelemetryResource.class);
        router.attach(DeviceVariableChangeIdsResource.PATH, DeviceVariableChangeIdsResource.class);
        router.attach(DevicesResource.PATH, DevicesResource.class);
        router.attach(DeviceVariableResource.PATH, DeviceVariableResource.class);
        router.attach(DeviceVariablesResource.PATH, DeviceVariablesResource.class);
        router.attach(EnableDeviceTelemetryResource.PATH, EnableDeviceTelemetryResource.class);
        router.attach(ExecuteTaskResource.PATH, ExecuteTaskResource.class);
        router.attach(GlobalVariableResource.PATH, GlobalVariableResource.class);
        router.attach(GlobalVariablesResource.PATH, GlobalVariablesResource.class);
        router.attach(HubConfigurationResource.PATH, HubConfigurationResource.class);
        router.attach(HubImageResource.PATH, HubImageResource.class);
        router.attach(HubPasswordResource.PATH, HubPasswordResource.class);
        router.attach(HubSendTestEmailResource.PATH, HubSendTestEmailResource.class);
        router.attach(ImageLibraryGroupResource.PATH, ImageLibraryGroupResource.class);
        router.attach(ImageLibraryImageResource.PATH, ImageLibraryImageResource.class);
        router.attach(ImageLibraryRootResource.PATH, ImageLibraryRootResource.class);
        router.attach(LogResource.PATH, LogResource.class);
        router.attach(MediaProxyResource.PATH, MediaProxyResource.class);
        router.attach(PluginConfigurationResource.PATH, PluginConfigurationResource.class);
        router.attach(PluginCurrentVersionResource.PATH, PluginCurrentVersionResource.class);
        router.attach(PluginDevicesResource.PATH, PluginDevicesResource.class);
        router.attach(PluginIconResource.PATH, PluginIconResource.class);
        router.attach(PluginInstallResource.PATH, PluginInstallResource.class);
        router.attach(PluginReloadResource.PATH, PluginReloadResource.class);
        router.attach(PluginResource.PATH, PluginResource.class);
        router.attach(PluginsResource.PATH, PluginsResource.class);
        router.attach(PresenceEntitiesResource.PATH, PresenceEntitiesResource.class);
        router.attach(ShutdownResource.PATH, ShutdownResource.class);
        router.attach(TaskResource.PATH, TaskResource.class);
        router.attach(TasksResource.PATH, TasksResource.class);

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

        authenticator.setNext(cache);

        // make the authenticator the first restlet in the chain
        return authenticator;
    }

    public String getApiRoot() {
        return PATH;
    }
}
